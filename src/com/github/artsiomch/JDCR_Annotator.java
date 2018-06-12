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
import java.util.Optional;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*;

public class JDCR_Annotator implements Annotator {
  private List<TextRange> htmlTags;

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    //TODO activate only if Folding enebled
    if (element instanceof PsiDocToken) {
      htmlTags = JDCR_StringUtils.getCombinedHtmlTags(element.getText());
      if (htmlTags.isEmpty()) return;

      getTextRangesForHtmlTags( Arrays.asList(
              new Tag("<b>", "</b>"),
              new Tag("<i>", "</i>")), element)
              .forEach( textRange -> {
        TextAttributes textAttributes = DOC_COMMENT.getDefaultAttributes().clone();
        /** See {@link java.awt.Font} Set or revert FontType*/
        textAttributes.setFontType( textAttributes.getFontType() ^ Font.BOLD);
        holder.createInfoAnnotation( textRange, null)
                .setEnforcedTextAttributes( textAttributes);
      });
      getTextRangesForHtmlTags( Arrays.asList(
              new Tag("<code>", "</code>"),
              new Tag("<tt>", "</tt>")), element)
              .forEach( textRange ->
                      holder.createInfoAnnotation( textRange, null)
                              .setTextAttributes( JDCR_ColorSettingsPage.CODE_TAG));

    } else if (element instanceof PsiInlineDocTag && ((PsiInlineDocTag) element).getName().equals("code")) {
      // fixme: If that func style really better?! :-/
      Optional<PsiElement> dataElement = Arrays.stream(((PsiInlineDocTag) element).getDataElements())
              .filter( it -> it.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA)
              .findFirst();
      dataElement.ifPresent(psiElement ->
              holder.createInfoAnnotation(new TextRange(psiElement.getTextOffset(), element.getTextRange().getEndOffset() - 1), null)
              .setTextAttributes(JDCR_ColorSettingsPage.CODE_TAG));

    } else if (element instanceof PsiDocMethodOrFieldRef ) { // @link
      holder.createInfoAnnotation( new TextRange( element.getTextOffset(), element.getTextRange().getEndOffset()), null)
              .setTextAttributes( JDCR_ColorSettingsPage.LINK_TAG);

    }
  }

  private List<TextRange> getTextRangesForHtmlTags(@NotNull List<Tag> tags,
                                                   @NotNull PsiElement element) {
    List<TextRange> result = new ArrayList<>();
    int pos = element.getTextRange().getStartOffset();
    int start = -2, end = -2;
    for (Tag tag : tags) {
      for (TextRange textRange : htmlTags) {
        if (textRange.substring(element.getText()).contains( tag.open)) {
          start = textRange.getEndOffset();
        }
        if (textRange.substring(element.getText()).contains( tag.close)) {
          end = textRange.getStartOffset();
        }
        if (start != -2 && end != -2 && start < end) {
//        assert start <= end : "Start="+start+" End="+end+" at: "+element.getText();
          TextRange newTextRange = new TextRange(pos + start, pos + end);
          // exclude dublicates
          if (result.stream().noneMatch( it -> it.contains( newTextRange)))
            result.add( newTextRange);
          start = -2;
          end = -2;
        }
      }
    }
    return result;
  }

  private class Tag {
    public Tag (@NotNull String openTag, @NotNull String closeTag){
      this.open = openTag;
      this.close = closeTag;
    }
    @NotNull String open;
    @NotNull String close;
  }

}
