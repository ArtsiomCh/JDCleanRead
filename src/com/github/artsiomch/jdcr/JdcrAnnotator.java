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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JdcrAnnotator implements Annotator {
  private AnnotationHolder holder;

  private static final Tag CODE_TAG = new Tag("<code>", "</code>");
  private static final Tag TT_TAG = new Tag("<tt>", "</tt>");
  private static final Tag PRE_TAG = new Tag("<pre>", "</pre>");
  private static final Tag A_HREF_TAG = new Tag("<a href=", "</a>");
  private static final Tag A_NAME_TAG = new Tag("<a name=", "</a>");
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
      annotateTag((PsiDocToken) element, A_HREF_TAG, JdcrColorSettingsPage.HTML_LINK_TAG);

      // Annotate <a name=...> tags
      annotateTag((PsiDocToken) element, A_NAME_TAG, JdcrColorSettingsPage.BOLD_FONT);

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
    JdcrPsiTreeUtils.getMultiLineTagRangesInParent(element)
        .forEach(
            range ->
                doAnnotate(
                    range.shiftRight(parent.getTextRange().getStartOffset()),
                    DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP));
  }

  private void annotateTag(
      @NotNull PsiDocToken element, Tag tag, @NotNull TextAttributesKey textAttributesKey) {
    ArrayList<TextRange> rangesToAnnotate = new ArrayList<>();

    for (TextRange tagValue : JdcrStringUtils.getValuesOfTag(element.getText(), tag)) {
      if (tagValue.getEndOffset() == element.getTextLength()) {
        // lonely open tag found withing current PsiDocToken
        // possible start of multiline value of tag.
        int tagValueStartInParent = tagValue.getStartOffset() + element.getStartOffsetInParent();
        rangesToAnnotate.addAll(getTagValueRanges(element, tag, tagValueStartInParent));
      } else if (tagValue.getStartOffset() != 0) {
        // don't annotate lonely close tag, should be covered in above case
        rangesToAnnotate.add(tagValue.shiftRight(element.getTextRange().getStartOffset()));
      }
    }
    // Check for Multi-line open tag.
    LinkedList<TextRange> multiLineTagRangesInParent =
        JdcrPsiTreeUtils.getMultiLineTagRangesInParent(element);
    if (!multiLineTagRangesInParent.isEmpty()
        && tag.openIn(getMultilineTagText(element.getParent(), multiLineTagRangesInParent))) {
      int tagValueStartInParent = multiLineTagRangesInParent.getLast().getEndOffset();
      rangesToAnnotate.addAll(getTagValueRanges(element, tag, tagValueStartInParent));
    }

    rangesToAnnotate.forEach(range -> doAnnotate(range, textAttributesKey));
  }

  private String getMultilineTagText(
      @NotNull PsiElement parent, @NotNull LinkedList<TextRange> multilineTagRangesInParent) {
    return multilineTagRangesInParent
        .stream()
        .map(range -> range.substring(parent.getText()))
        .reduce("", String::concat);
  }

  private static final List<TextRange> EMPTY_ARRAY = new ArrayList<>();
  /**
   * Look ahead for close tag.
   *
   * @param element to start from
   * @param tag to check
   * @param tagValueStartInParent offset <i>relatively</i> to {@code element.getParent()}
   * @return ranges of Tag Value <i>relatively</i> to {@code element.getParent()}
   */
  private List<TextRange> getTagValueRanges(
      @NotNull PsiDocToken element, Tag tag, int tagValueStartInParent) {

    int tagValueEndInParent = -1;
    PsiElement inspectingElement = element;
    while (inspectingElement != null && tagValueEndInParent == -1) {
      if (inspectingElement.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA) {

        int startOffsetInParent = inspectingElement.getStartOffsetInParent();
        tagValueEndInParent =
            JdcrStringUtils.getValuesOfTag(inspectingElement.getText(), tag)
                .stream()
                .filter(range1 -> range1.getStartOffset() == 0)
                // close tag found
                .findFirst()
                .map(TextRange::getEndOffset)
                .map(endOffsetInsideInspecting -> endOffsetInsideInspecting + startOffsetInParent)
                .filter(endOffsetInParent -> tagValueStartInParent <= endOffsetInParent)
                // </tag>...<tag> case
                .orElse(-1);

        if (tagValueEndInParent == -1) {
          // Check for Multi-line close tag.
          LinkedList<TextRange> multiLineTagRangesInParent =
              JdcrPsiTreeUtils.getMultiLineTagRangesInParent(inspectingElement);
          if (!multiLineTagRangesInParent.isEmpty()
              && tag.closeIn(
                  getMultilineTagText(element.getParent(), multiLineTagRangesInParent))) {
            tagValueEndInParent = multiLineTagRangesInParent.getFirst().getStartOffset();
          }
        }
      }
      inspectingElement = inspectingElement.getNextSibling();
    }

    return (tagValueEndInParent == -1)
        ? EMPTY_ARRAY
        : JdcrPsiTreeUtils.excludeLineBreaks(
                element.getParent(), new TextRange(tagValueStartInParent, tagValueEndInParent))
            .stream()
            .map(range -> range.shiftRight(element.getParent().getTextRange().getStartOffset()))
            .collect(Collectors.toCollection(ArrayList::new));
  }

  private void doAnnotate(
      @NotNull TextRange absoluteRange, @NotNull TextAttributesKey textAttributesKey) {
    holder.createInfoAnnotation(absoluteRange, null).setTextAttributes(textAttributesKey);
  }

  private void doAnnotate(
      @NotNull PsiElement element, @NotNull TextAttributesKey textAttributesKey) {
    holder.createInfoAnnotation(element, null).setTextAttributes(textAttributesKey);
  }
}
