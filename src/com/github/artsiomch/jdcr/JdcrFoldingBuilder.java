package com.github.artsiomch.jdcr;

import com.github.artsiomch.jdcr.utils.JdcrPsiTreeUtils;
import com.github.artsiomch.jdcr.utils.JdcrStringUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.JavaDocElementType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Arrays;
import java.util.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.parser.Parser;

public class JdcrFoldingBuilder implements FoldingBuilder {

  private Stack<FoldingDescriptor> foldingDescriptors;
  private FoldingGroup foldingGroup;
  private static final int LENGTH_DOC_INLINE_TAG_END = 1; // }

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    PsiElement root = node.getPsi();
    foldingDescriptors = new Stack<>();

    //    long startTime= System.currentTimeMillis();
    for (PsiDocComment psiDocComment : PsiTreeUtil.findChildrenOfType(root, PsiDocComment.class)) {
      foldingGroup = FoldingGroup.newGroup("JDCR " + psiDocComment.getTokenType().toString());

      PsiTreeUtil.findChildrenOfType(psiDocComment, PsiDocToken.class)
          .stream()
          .filter(psiDocToken -> !JdcrPsiTreeUtils.isInsideCodeOrLiteralTag(psiDocToken))
          .forEach(this::checkHtmlTagsAndEscapedChars);

      PsiTreeUtil.findChildrenOfType(psiDocComment, PsiInlineDocTag.class)
          .forEach(this::checkInlineJavaDocTags);
    }
    /*
        System.out.printf("File: %-20s  Folding time: %6d,  Total folds created: %6d\n",
            root.getContainingFile().getName(),
            (System.currentTimeMillis() - startTime),
            foldingDescriptors.capacity());
    */
    return foldingDescriptors.toArray(new FoldingDescriptor[0]);
  }

  /**
   * Add FoldingDescriptors for inline JavaDoc tags: {@link JdcrStringUtils#CODE_TAGS} {@link
   * JdcrStringUtils#LINK_TAGS}
   */
  private void checkInlineJavaDocTags(@NotNull PsiInlineDocTag psiInlineDocTag) {
    String tagName = psiInlineDocTag.getName();
    if (JdcrStringUtils.CODE_TAGS.contains(tagName)) {
      foldJavaDocTagStartEnd(psiInlineDocTag);
    } else if (JdcrStringUtils.LINK_TAGS.contains(tagName)) {
      foldJavaDocTagStartEnd(psiInlineDocTag);

      // Folding label part of @link tag
      Arrays.stream(psiInlineDocTag.getChildren())
          .filter(
              child ->
                  // link through # within current file
                  (child.getNode().getElementType() == JavaDocElementType.DOC_METHOD_OR_FIELD_REF
                          // link to outer file
                          || child.getNode().getElementType()
                              == JavaDocElementType.DOC_REFERENCE_HOLDER)
                      // @link tag has text label part to fold
                      && child.getNextSibling() != psiInlineDocTag.getLastChild())
          .findFirst()
          .map(
              linkToShow ->
                  new TextRange(
                      linkToShow.getTextRange().getEndOffset()
                          - psiInlineDocTag.getTextRange().getStartOffset(),
                      psiInlineDocTag.getTextLength() - LENGTH_DOC_INLINE_TAG_END))
          .ifPresent(
              labelToFold ->
                  JdcrPsiTreeUtils.excludeLineBreaks(psiInlineDocTag, labelToFold)
                      .forEach(range -> addFoldingDescriptor(psiInlineDocTag, range)));
    }
  }

  private void foldJavaDocTagStartEnd(@NotNull PsiInlineDocTag psiInlineDocTag) {
    // fold JavaDoc tag Start
    JdcrPsiTreeUtils.excludeLineBreaks(
            psiInlineDocTag,
            new TextRange(
                0, 2 /* `{@` */ + psiInlineDocTag.getName().length() + 1 /* space after */))
        .forEach(range -> addFoldingDescriptor(psiInlineDocTag, range));
    // fold JavaDoc tag End
    addFoldingDescriptor(
        psiInlineDocTag,
        new TextRange(
            psiInlineDocTag.getTextLength() - LENGTH_DOC_INLINE_TAG_END,
            psiInlineDocTag.getTextLength()));
  }

  /** Add FoldingDescriptors for HTML tags and Escaped Chars */
  private void checkHtmlTagsAndEscapedChars(@NotNull PsiDocToken psiDocToken) {
    JdcrStringUtils.getHtmlTags(psiDocToken.getText())
        .forEach(
            textRange -> {
              String tagsToFold = textRange.substring(psiDocToken.getText()).toLowerCase();
              if (tagsToFold.contains("<li>")) {
                addFoldingDescriptor(psiDocToken, textRange, " - ");
                //              } else if (tagsToFold.contains("<td>")) {
                //                addFoldingDescriptor(psiDocToken, textRange, "\t");
              } else {
                addFoldingDescriptor(psiDocToken, textRange);
              }
            });

    // Check for Multi-line tag.
    JdcrPsiTreeUtils.getMultiLineTagRangesInParent(psiDocToken)
        .forEach(textRange -> addFoldingDescriptor(psiDocToken.getParent(), textRange));

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

    // reducing folding regions amount by joint sequential regions into one: <i><b>...
    TextRange absoluteNewRange = range.shiftRight(element.getTextRange().getStartOffset());
    if (!foldingDescriptors.empty()
        && foldingDescriptors.peek().getRange().getEndOffset()
            == absoluteNewRange.getStartOffset()) {
      FoldingDescriptor prevFoldingDescriptor = foldingDescriptors.pop();
      absoluteNewRange =
          new TextRange(
              prevFoldingDescriptor.getRange().getStartOffset(), absoluteNewRange.getEndOffset());
      placeholderText = prevFoldingDescriptor.getPlaceholderText() + placeholderText;
    }

    foldingDescriptors.push(
        new NamedFoldingDescriptor(
            element.getNode(), absoluteNewRange, foldingGroup, placeholderText));
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
