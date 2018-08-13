package com.github.artsiomch.jdcr;

import com.github.artsiomch.jdcr.utils.JdcrStringUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiAnnotatedJavaCodeReferenceElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.LinkedList;
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

  /** Add FoldingDescriptors for inline JavaDoc tags: @code and @link */
  private void checkInlineJavaDocTags(@NotNull PsiInlineDocTag psiInlineDocTag) {
    if (psiInlineDocTag.getName().equals("code")) {
      foldJavaDocTagStartEnd(psiInlineDocTag);
    } else if (psiInlineDocTag.getName().equals("link")) {
      foldJavaDocTagStartEnd(psiInlineDocTag);

      // Folding label part of @link tag
      PsiElement linkToShow =
          PsiTreeUtil.findChildOfAnyType(
              psiInlineDocTag,
              PsiDocMethodOrFieldRef.class, // link through # within current file
              PsiAnnotatedJavaCodeReferenceElement.class); // link to outer file
      if (linkToShow != null
          // @link tag has text label part to fold
          && linkToShow.getNextSibling() != psiInlineDocTag.getLastChild()) {
        TextRange labelToFold =
            new TextRange(
                linkToShow.getTextRange().getEndOffset()
                    - psiInlineDocTag.getTextRange().getStartOffset(),
                psiInlineDocTag.getTextLength() - 1);
        getRangesToFold(psiInlineDocTag, labelToFold)
            .forEach(range -> addFoldingDescriptor(psiInlineDocTag, range));
      }
    }
  }

  private void foldJavaDocTagStartEnd(@NotNull PsiInlineDocTag psiInlineDocTag) {
    // fold JavaDoc tag Start
    addFoldingDescriptor(psiInlineDocTag, new TextRange(0, 2 + psiInlineDocTag.getName().length()));
    // fold JavaDoc tag End
    addFoldingDescriptor(
        psiInlineDocTag,
        new TextRange(psiInlineDocTag.getTextLength() - 1, psiInlineDocTag.getTextLength()));
  }

  /**
   * Look inside {@code range} in JavaDoc {@code element} for line breaks
   *
   * @param element
   * @param range
   * @return List of TextRanges between line breaks inside {@code range}
   */
  private List<TextRange> getRangesToFold(@NotNull PsiElement element, @NotNull TextRange range) {
    List<TextRange> result = new LinkedList<>();
    int prevLineBreak = range.getStartOffset();
    for (PsiElement ws : element.getChildren()) {
      if (ws instanceof PsiWhiteSpace
          && ws.getNextSibling() != null
          && ws.getNextSibling().getNode().getElementType()
              == JavaDocTokenType.DOC_COMMENT_LEADING_ASTERISKS
          && range.contains(ws.getStartOffsetInParent())) {
        if (ws.getStartOffsetInParent() > prevLineBreak) {
          result.add(new TextRange(prevLineBreak, ws.getStartOffsetInParent()));
        }
        prevLineBreak = ws.getNextSibling().getStartOffsetInParent() + 1;
      }
    }
    result.add(new TextRange(prevLineBreak, range.getEndOffset()));
    return result;
  }

  /** Add FoldingDescriptors for HTML tags and Escaped Chars */
  private void checkHtmlTagsAndEscapedChars(@NotNull PsiDocToken psiDocToken) {
    if (psiDocToken.getParent() instanceof PsiInlineDocTag
        && ((PsiInlineDocTag) psiDocToken.getParent()).getName().equals("code"))
      // PsiDocToken inside @code tag -> do not interpreting the text as HTML markup
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

  private void addFoldingDescriptor(@NotNull PsiElement element, @NotNull TextRange range) {
    addFoldingDescriptor(element, range, ""); // "◊"
  }

  private void addFoldingDescriptor(
      @NotNull PsiElement element, @NotNull TextRange range, String placeholderText) {
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
