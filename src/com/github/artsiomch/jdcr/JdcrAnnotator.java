package com.github.artsiomch.jdcr;

import com.github.artsiomch.jdcr.utils.JdcrPsiTreeUtils;
import com.github.artsiomch.jdcr.utils.JdcrStringUtils;
import com.github.artsiomch.jdcr.utils.Tag;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class JdcrAnnotator implements Annotator {
  private AnnotationHolder holder;

  private static final List<Tag> CODE_TAGS =
      Arrays.asList(new Tag("<code>", "</code>"), new Tag("<tt>", "</tt>"));

  private static final List<Tag> HTML_LINK_TAGS = Arrays.asList(new Tag("<a href=\"", "</a>"));

  private static final List<Tag> BOLD_TAGS = Arrays.asList(new Tag("<b>", "</b>"));

  private static final List<Tag> ITALIC_TAGS = Arrays.asList(new Tag("<i>", "</i>"));

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!JdcrPsiTreeUtils.isJavaDocElement(element)) return;
    this.holder = holder;
    if (element instanceof PsiDocToken
        && ((PsiDocToken) element).getTokenType() == JavaDocTokenType.DOC_COMMENT_DATA
        && !JdcrPsiTreeUtils.isInsideCodeOrLiteralTag((PsiDocToken) element)) {
      // Annotate Font style HTML tags
      annotateAllTagsWithTextAttributes(
          (PsiDocToken) element, BOLD_TAGS, JdcrColorSettingsPage.BOLD_FONT);
      annotateAllTagsWithTextAttributes(
          (PsiDocToken) element, ITALIC_TAGS, JdcrColorSettingsPage.ITALIC_FONT);
      // Annotate Code HTML tags
      annotateAllTagsWithTextAttributes(
          (PsiDocToken) element, CODE_TAGS, JdcrColorSettingsPage.CODE_TAG);
      // Annotate HTML link tags
      annotateAllTagsWithTextAttributes(
          (PsiDocToken) element, HTML_LINK_TAGS, JdcrColorSettingsPage.HTML_LINK_TAG);
    } else if (element instanceof PsiInlineDocTag
        && ((PsiInlineDocTag) element).getName().equals("code")) { // @code
      annotateCodeAnnotations((PsiInlineDocTag) element);
    } else if (element instanceof PsiDocMethodOrFieldRef) { // @link @linkplain @value
      annotateLinkAnnotations((PsiDocMethodOrFieldRef) element);
    }
    this.holder = null;
  }

  private void annotateLinkAnnotations(@NotNull PsiDocMethodOrFieldRef element) {
    holder
        .createInfoAnnotation(
            new TextRange(element.getTextOffset(), element.getTextRange().getEndOffset()), null)
        .setTextAttributes(JdcrColorSettingsPage.LINK_TAG);
  }

  private void annotateCodeAnnotations(@NotNull PsiInlineDocTag element) {
    Arrays.stream(element.getDataElements())
        .filter(it -> it.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA)
        .forEach(
            psiElement ->
                holder
                    .createInfoAnnotation(psiElement, null)
                    .setTextAttributes(JdcrColorSettingsPage.CODE_TAG));
  }

  private void annotateAllTagsWithTextAttributes(
      @NotNull PsiDocToken element, List<Tag> tagsToAnnotate, TextAttributesKey textAttributesKey) {
    for (TextRange textRange :
        JdcrStringUtils.getTextRangesForHtmlTagValues(element.getText(), tagsToAnnotate)) {
      holder
          .createInfoAnnotation(textRange.shiftRight(element.getTextRange().getStartOffset()), null)
          .setTextAttributes(textAttributesKey);
    }
  }
}
