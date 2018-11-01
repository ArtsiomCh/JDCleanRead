package com.github.artsiomch.jdcr;

import com.github.artsiomch.jdcr.utils.JdcrPsiTreeUtils;
import com.github.artsiomch.jdcr.utils.JdcrStringUtils;
import com.github.artsiomch.jdcr.utils.Tag;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.javadoc.PsiDocMethodOrFieldRef;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import java.util.LinkedList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JdcrAnnotator implements Annotator {
  private AnnotationHolder holder;

  private static final Tag CODE_TAG = new Tag("<code>", "</code>");
  private static final Tag TT_TAG = new Tag("<tt>", "</tt>");
  private static final Tag PRE_TAG = new Tag("<pre>", "</pre>");
  private static final Tag HTML_LINK_TAG = new Tag("<a", "</a>");
  private static final Tag BOLD_TAG = new Tag("<b>", "</b>");
  private static final Tag ITALIC_TAG = new Tag("<i>", "</i>");
  private static final Tag EM_TAG = new Tag("<em>", "</em>");

  @Override
  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    this.holder = holder;

    if (element instanceof PsiDocToken
        && ((PsiDocToken) element).getTokenType() == JavaDocTokenType.DOC_COMMENT_DATA
        && !JdcrPsiTreeUtils.isInsideCodeOrLiteralTag((PsiDocToken) element)) {

      // Annotate Font style HTML tags
      annotateTag((PsiDocToken) element, BOLD_TAG, JdcrColorSettingsPage.BOLD_FONT);
      annotateTag((PsiDocToken) element, ITALIC_TAG, JdcrColorSettingsPage.ITALIC_FONT);
      annotateTag((PsiDocToken) element, EM_TAG, JdcrColorSettingsPage.ITALIC_FONT);

      // Annotate Code HTML tags
      annotateTag((PsiDocToken) element, CODE_TAG, JdcrColorSettingsPage.CODE_TAG);
      annotateTag((PsiDocToken) element, TT_TAG, JdcrColorSettingsPage.CODE_TAG);
      annotateTag((PsiDocToken) element, PRE_TAG, JdcrColorSettingsPage.CODE_TAG);

      // Annotate HTML link tags
      annotateTag((PsiDocToken) element, HTML_LINK_TAG, JdcrColorSettingsPage.HTML_LINK_TAG);

      // Annotate multiline Tag, fix https://youtrack.jetbrains.com/issue/IDEA-198738
      annotateMultiLineTag((PsiDocToken) element);

    } else if (element instanceof PsiInlineDocTag
        && ((PsiInlineDocTag) element).getName().equals("code")) { // @code
      annotateCodeAnnotations((PsiInlineDocTag) element);

    } else if (element instanceof PsiDocMethodOrFieldRef) { // @link @linkplain @value
      annotateLinkAnnotations((PsiDocMethodOrFieldRef) element);
    }
    this.holder = null;
  }

  private void annotateLinkAnnotations(@NotNull PsiDocMethodOrFieldRef element) {
    doAnnotate(
        new TextRange(element.getTextOffset(), element.getTextRange().getEndOffset()),
        JdcrColorSettingsPage.LINK_TAG);
  }

  private void annotateCodeAnnotations(@NotNull PsiInlineDocTag element) {
    Arrays.stream(element.getDataElements())
        .filter(dataElm -> dataElm.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA)
        .forEach(commentData -> doAnnotate(commentData, JdcrColorSettingsPage.CODE_TAG));
  }

  private void annotateMultiLineTag(@NotNull PsiDocToken element) {
    PsiElement parent = element.getParent();
    JdcrPsiTreeUtils.getMultiLineTag(element)
        .forEach(
            range ->
                doAnnotate(
                    range.shiftRight(parent.getTextRange().getStartOffset()),
                    DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP));
  }

  private void annotateTag(
      @NotNull PsiDocToken element, Tag tag, @NotNull TextAttributesKey textAttributesKey) {

    for (TextRange tagValue : JdcrStringUtils.getValuesOfTag(element.getText(), tag)) {

      if (tagValue.getStartOffset() == 0) { // lonely close tag found withing current PsiDocToken
        // possible end of multiline tag value. Look for open tag.
        int tagValueEnd = tagValue.getEndOffset() + element.getStartOffsetInParent();
        int tagValueStart = getTagValueStart(element, tag, tagValueEnd);
        if (tagValueStart != -1) {
          JdcrPsiTreeUtils.excludeLineBreaks(
                  element.getParent(), new TextRange(tagValueStart, tagValueEnd))
              .forEach(
                  range ->
                      doAnnotate(
                          range.shiftRight(element.getParent().getTextRange().getStartOffset()),
                          textAttributesKey));
        }
      } else if (tagValue.getEndOffset() != element.getTextLength()) {
        // don't annotate lonely open tag, should be covered in above case
        doAnnotate(tagValue.shiftRight(element.getTextRange().getStartOffset()), textAttributesKey);
      }
    }
  }

  private void doAnnotate(
      @NotNull TextRange absoluteRange, @NotNull TextAttributesKey textAttributesKey) {
    holder.createInfoAnnotation(absoluteRange, null).setTextAttributes(textAttributesKey);
  }

  private void doAnnotate(
      @NotNull PsiElement element, @NotNull TextAttributesKey textAttributesKey) {
    holder.createInfoAnnotation(element, null).setTextAttributes(textAttributesKey);
  }

  /**
   * Look behind for open tag.
   *
   * @param element
   * @param tag
   * @param tagValueEnd offset <i>relatively</i> to {@code element.getParent()}
   * @return TagValueStart offset <i>relatively</i> to {@code element.getParent()}
   */
  private int getTagValueStart(@NotNull PsiDocToken element, Tag tag, int tagValueEnd) {
    int tagValueStart = -1;
    PsiElement inspectingElement = element;
    final PsiElement parent = element.getParent();
    while (inspectingElement != null && tagValueStart == -1) {
      if (inspectingElement.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA) {
        int inspectingLength = inspectingElement.getTextLength();
        int startOffsetInParent = inspectingElement.getStartOffsetInParent();

        tagValueStart =
            JdcrStringUtils.getValuesOfTag(inspectingElement.getText(), tag)
                .stream()
                .filter(range -> range.getEndOffset() == inspectingLength)
                // open tag found
                .findFirst()
                .map(TextRange::getStartOffset)
                .map(i -> i + startOffsetInParent)
                .filter(i -> i <= tagValueEnd)
                // </tag>...<tag> case
                .orElse(-1);

        if (tagValueStart == -1) {
          // Check for Multi-line tag.
          LinkedList<TextRange> multiLineTag = JdcrPsiTreeUtils.getMultiLineTag(inspectingElement);
          if (multiLineTag
              .stream()
              .map(range -> range.substring(parent.getText()))
              .reduce("", String::concat)
              .replace(" ", "")
              .contains(tag.open)) {
            tagValueStart = multiLineTag.getLast().getEndOffset();
          }
        }
      }
      inspectingElement = inspectingElement.getPrevSibling();
    }
    return tagValueStart;
  }
}
