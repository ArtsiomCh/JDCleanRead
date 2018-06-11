package com.github.artsiomch;

import com.github.artsiomch.utils.JDCRStringUtils;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import org.intellij.lang.annotations.JdkConstants;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*;

public class JDCRAnnotator implements Annotator {
  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    //TODO activate only if Folding enebled
    if (element instanceof PsiDocToken) {
      getTextRangesForHtmlTag( "<b>", "</b>",  element).forEach( textRange -> {
        TextAttributes textAttributes = createTextAttributes( DOC_COMMENT);
        /** See {@link java.awt.Font} Set or revert FontType*/
        textAttributes.setFontType( textAttributes.getFontType() ^ Font.BOLD);
        holder.createInfoAnnotation( textRange, null)
                .setEnforcedTextAttributes( textAttributes);
      });
      getTextRangesForHtmlTag( "<code>", "</code>",  element).forEach( textRange -> {
        TextAttributes textAttributes = createTextAttributes( DOC_COMMENT_MARKUP);
//      textAttributes.setEffectType( EffectType.WAVE_UNDERSCORE);
        holder.createInfoAnnotation( textRange, null)
                .setEnforcedTextAttributes( textAttributes);
      });
      getTextRangesForHtmlTag( "<tt>", "</tt>",  element).forEach( textRange ->
              holder.createInfoAnnotation( textRange, null)
                      .setEnforcedTextAttributes( createTextAttributes( DOC_COMMENT_MARKUP)));
//    getTextRangesForHtmlTag( "<i>", "</i>", Font.ITALIC, DOC_COMMENT, element, holder);

    } else if (element instanceof PsiInlineDocTag && ((PsiInlineDocTag) element).getName().equals("code")) {
      TextAttributes textAttributes = createTextAttributes(DOC_COMMENT_MARKUP);
      holder.createInfoAnnotation( element, null).setEnforcedTextAttributes( textAttributes);
    } else if (element instanceof PsiDocMethodOrFieldRef ) { // @link
      TextAttributes textAttributes = createTextAttributes(CLASS_NAME);
      holder.createInfoAnnotation( element, null).setEnforcedTextAttributes( textAttributes);
    }
  }

  private List<TextRange> getTextRangesForHtmlTag(@NotNull String openTag,
                                                  @NotNull String closeTag,
                                                  @NotNull PsiElement element) {
    List<TextRange> result = new ArrayList<>();
    int pos = element.getTextRange().getStartOffset();
    int start = -2, end = -2;
    for (TextRange textRange : JDCRStringUtils.getCombinedHtmlTags(element.getText())) {
      if (textRange.substring(element.getText()).contains( openTag)) {
        start = textRange.getEndOffset();
      }
      if (textRange.substring(element.getText()).contains( closeTag)) {
        end = textRange.getStartOffset();
      }
      if (start != -2 && end != -2 && start < end) {
//        assert start <= end : "Start="+start+" End="+end+" at: "+element.getText();
        result.add( new TextRange(pos + start, pos + end));
        start = -2;
        end = -2;
      }
    }
    return result;
  }

  private TextAttributes createTextAttributes (TextAttributesKey baseTextAttributesKey) {
    return baseTextAttributesKey.getDefaultAttributes().clone();
  }

}
