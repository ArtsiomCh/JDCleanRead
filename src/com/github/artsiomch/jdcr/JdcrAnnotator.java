package com.github.artsiomch.jdcr;

import com.github.artsiomch.jdcr.utils.JdcrStringUtils;
import com.github.artsiomch.jdcr.utils.Tag;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import org.jetbrains.annotations.NotNull;

import java.awt.Font;
import java.util.Arrays;
import java.util.List;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*;

public class JdcrAnnotator implements Annotator {
  private AnnotationHolder holder;

  private static final List<Tag> CODE_TAGS =
      Arrays.asList(new Tag("<code>", "</code>"), new Tag("<tt>", "</tt>"));

  private static final List<Tag> FONT_STYLE_TAGS =
      Arrays.asList(new Tag("<b>", "</b>"), new Tag("<i>", "</i>"));

  private static final TextAttributes FONT_STYLE_TEXT_ATTRIBUTES = setFontStyleTextAttributes();
  // fixme
  private static TextAttributes setFontStyleTextAttributes() {
    TextAttributes textAttributes = DOC_COMMENT.getDefaultAttributes().clone();
    /** Set or revert FontType. See {@link Font} */
    textAttributes.setFontType(textAttributes.getFontType() ^ Font.BOLD);
    return textAttributes;
  }

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    // TODO activate only if Folding enabled
    this.holder = holder;
    if (element instanceof PsiDocToken) {
      // Annotate Font style HTML tags
      annotateAllTagsWithTextAttributes(
          (PsiDocToken) element, FONT_STYLE_TAGS, FONT_STYLE_TEXT_ATTRIBUTES);
      // Annotate Code HTML tags
      annotateAllTagsWithTextAttributes(
          (PsiDocToken) element, CODE_TAGS, JdcrColorSettingsPage.CODE_TAG.getDefaultAttributes());
    } else if (element instanceof PsiInlineDocTag
        && ((PsiInlineDocTag) element).getName().equals("code")) { // @code
      annotateCodeAnnotations((PsiInlineDocTag) element);
    } else if (element instanceof PsiDocMethodOrFieldRef) { // @link
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
        .findFirst()
        .ifPresent(
            psiElement ->
                holder
                    .createInfoAnnotation(
                        new TextRange(
                            psiElement.getTextOffset(), element.getTextRange().getEndOffset() - 1),
                        null)
                    .setTextAttributes(JdcrColorSettingsPage.CODE_TAG));
  }

  private void annotateAllTagsWithTextAttributes(
      @NotNull PsiDocToken element, List<Tag> tagsToAnnotate, TextAttributes textAttributes) {
    for (TextRange textRange :
        JdcrStringUtils.getTextRangesForHtmlTags(element.getText(), tagsToAnnotate)) {
      holder
          .createInfoAnnotation(textRange.shiftRight(element.getTextRange().getStartOffset()), null)
          .setEnforcedTextAttributes(textAttributes);
    }
  }
}
