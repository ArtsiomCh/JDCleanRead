package com.github.artsiomch.jdcr;

import com.github.artsiomch.jdcr.utils.JdcrStringUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiAnnotatedJavaCodeReferenceElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public class JdcrFoldingBuilder implements FoldingBuilder {

  private List<FoldingDescriptor> foldingDescriptors;
  private FoldingGroup foldingGroup;

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    PsiElement root = node.getPsi();
    foldingDescriptors = new ArrayList<>();

    for (PsiDocComment psiDocComment : PsiTreeUtil.findChildrenOfType(root, PsiDocComment.class)) {
      foldingGroup = FoldingGroup.newGroup("JDCR " + psiDocComment.getTokenType().toString());

      PsiTreeUtil.findChildrenOfType(psiDocComment, PsiDocToken.class)
          .forEach(this::checkHtmlTagsAndEscapedChars);

      PsiTreeUtil.findChildrenOfType(psiDocComment, PsiInlineDocTag.class)
          .forEach(this::checkInlineJavaDocTags);
    }
    return foldingDescriptors.toArray(new FoldingDescriptor[0]);
  }

  private static final TextRange textRangeTagStart = new TextRange(0, 6);

  /** Add FoldingDescriptors for inline JavaDoc tags: @code and @link */
  private void checkInlineJavaDocTags(PsiInlineDocTag psiInlineDocTag) {
    TextRange textRangeTagEnd = null;
    if (psiInlineDocTag.getName().equals("code")) {
      textRangeTagEnd =
          new TextRange(psiInlineDocTag.getTextLength() - 1, psiInlineDocTag.getTextLength());
    } else if (psiInlineDocTag.getName().equals("link")) {
      PsiElement psiDocLink = psiInlineDocTag.getValueElement();
      if (psiDocLink == null)
        psiDocLink =
            PsiTreeUtil.findChildOfType(
                psiInlineDocTag, PsiAnnotatedJavaCodeReferenceElement.class);
      if (psiDocLink != null) {
        textRangeTagEnd =
            new TextRange( // 7
                psiDocLink.getTextRange().getStartOffset()
                    - psiInlineDocTag.getTextRange().getStartOffset()
                    + psiDocLink.getTextLength(),
                psiInlineDocTag.getTextLength());
      }
    }
    if (textRangeTagEnd != null) {
      addFoldingDescriptor(psiInlineDocTag, textRangeTagStart);
      addFoldingDescriptor(psiInlineDocTag, textRangeTagEnd);
    }
  }

  /** Add FoldingDescriptors for HTML tags and Escaped Chars */
  private void checkHtmlTagsAndEscapedChars(PsiDocToken psiDocToken) {
    if (psiDocToken.getParent() instanceof PsiInlineDocTag
        && ((PsiInlineDocTag) psiDocToken.getParent()).getName().equals("code"))
      //PsiDocToken inside @code tag -> do not interpreting the text as HTML markup
      return;
    JdcrStringUtils.getCombinedHtmlTags(psiDocToken.getText())
        .forEach(
            textRange -> {
              if (textRange.substring(psiDocToken.getText()).toLowerCase().contains("<li>")) {
                addFoldingDescriptor(psiDocToken, textRange, " - ");
              } else {
                addFoldingDescriptor(psiDocToken, textRange);
              }
            });
    JdcrStringUtils.getHtmlEscapedChars(psiDocToken.getText())
        .forEach(
            textRange ->
                addFoldingDescriptor(
                    psiDocToken,
                    textRange,
                    Parser.unescapeEntities(textRange.substring(psiDocToken.getText()), true)));
  }

  private void addFoldingDescriptor(PsiElement element, TextRange range) {
    addFoldingDescriptor(element, range, ""); // "◊"
  }

  private void addFoldingDescriptor(PsiElement element, TextRange range, String placeholderText) {
    foldingDescriptors.add(
        new NamedFoldingDescriptor(
            element.getNode(),
            range.shiftRight(element.getTextRange().getStartOffset()),
            foldingGroup,
            placeholderText));
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    return null;
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return true;
  }
}
