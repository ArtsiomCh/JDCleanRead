package com.github.artsiomch;

import com.github.artsiomch.utils.JDCR_StringUtils;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*;

public class JDCR_Annotator implements Annotator {

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    //TODO activate only if Folding enabled
    if (element instanceof PsiDocToken) {
      List<TextRange> foundHtmlTags = JDCR_StringUtils.getCombinedHtmlTags(element.getText());
      if (foundHtmlTags.isEmpty()) {
        return;
      }
      annotateFontStyleTags(element, holder, foundHtmlTags);
      annotateCodeTags(element, holder, foundHtmlTags);
    } else if (element instanceof PsiInlineDocTag && ((PsiInlineDocTag) element).getName()
        .equals("code")) {
      annotateCodeAnnotations(element, holder);
    } else if (element instanceof PsiDocMethodOrFieldRef) { // @link
      annotateLinkAnnotations(element, holder);
    }
  }

  private void annotateLinkAnnotations(
      @NotNull PsiElement element,
      @NotNull AnnotationHolder holder) {
    holder.createInfoAnnotation(
        new TextRange(element.getTextOffset(), element.getTextRange().getEndOffset()), null)
        .setTextAttributes(JDCR_ColorSettingsPage.LINK_TAG);
  }

  private void annotateCodeAnnotations(
      @NotNull PsiElement element,
      @NotNull AnnotationHolder holder) {
    Arrays.stream(((PsiInlineDocTag) element).getDataElements())
        .filter(it -> it.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA)
        .findFirst()
        .ifPresent(psiElement -> {
          TextRange annotatedTextRange = new TextRange(psiElement.getTextOffset(),
              element.getTextRange().getEndOffset() - 1);
          holder.createInfoAnnotation(annotatedTextRange, null)
              .setTextAttributes(JDCR_ColorSettingsPage.CODE_TAG);
        });
  }

  private void annotateCodeTags(
      @NotNull PsiElement element,
      @NotNull AnnotationHolder holder,
      List<TextRange> foundHtmlTags) {
    List<Tag> tagsToAnnotate;
    tagsToAnnotate = Arrays.asList(
        new Tag("<code>", "</code>"),
        new Tag("<tt>", "</tt>"));
    annotateAllTagsWithTextAttributes(element, holder, foundHtmlTags, tagsToAnnotate,
        JDCR_ColorSettingsPage.CODE_TAG.getDefaultAttributes());
  }

  private void annotateFontStyleTags(
      @NotNull PsiElement element,
      @NotNull AnnotationHolder holder,
      List<TextRange> foundHtmlTags) {
    List<Tag> tagsToAnnotate = Arrays.asList(
        new Tag("<b>", "</b>"),
        new Tag("<i>", "</i>"));
    TextAttributes textAttributes = DOC_COMMENT.getDefaultAttributes().clone();
    /** See {@link Font} Set or revert FontType*/
    textAttributes.setFontType(textAttributes.getFontType() ^ Font.BOLD);
    annotateAllTagsWithTextAttributes(element, holder, foundHtmlTags, tagsToAnnotate,
        textAttributes);
  }

  private void annotateAllTagsWithTextAttributes(
      @NotNull PsiElement element,
      @NotNull AnnotationHolder holder,
      List<TextRange> foundHtmlTags,
      List<Tag> tagsToAnnotate,
      TextAttributes textAttributes) {
    for (TextRange textRange : getTextRangesForHtmlTags(tagsToAnnotate, foundHtmlTags, element)) {
      holder.createInfoAnnotation(textRange, null)
          .setEnforcedTextAttributes(textAttributes);
    }
  }

  private static final int EMPTY_INDEX = -2;

  private List<TextRange> getTextRangesForHtmlTags(
      @NotNull List<Tag> tagsToAnnotate,
      @NotNull List<TextRange> htmlTagsRanges,
      @NotNull PsiElement element) {
    List<TextRange> result = new ArrayList<>();
    int pos = element.getTextRange().getStartOffset();
    int start = EMPTY_INDEX, end = EMPTY_INDEX;
    for (Tag tag : tagsToAnnotate) {
      for (TextRange textRange : htmlTagsRanges) {
        if (textRange.substring(element.getText()).contains(tag.open)) {
          start = textRange.getEndOffset();
        }
        if (textRange.substring(element.getText()).contains(tag.close)) {
          end = textRange.getStartOffset();
        }
        if (start != EMPTY_INDEX && end != EMPTY_INDEX && start < end) {
//        assert start <= end : "Start="+start+" End="+end+" at: "+element.getText();
          TextRange newTextRange = new TextRange(pos + start, pos + end);
          // exclude dublicates
          if (result.stream().noneMatch(it -> it.contains(newTextRange))) {
            result.add(newTextRange);
          }
          start = EMPTY_INDEX;
          end = EMPTY_INDEX;
        }
      }
    }
    return result;
  }

  private class Tag {

    public Tag(@NotNull String openTag, @NotNull String closeTag) {
      this.open = openTag;
      this.close = closeTag;
    }

    @NotNull
    String open;
    @NotNull
    String close;
  }

}
