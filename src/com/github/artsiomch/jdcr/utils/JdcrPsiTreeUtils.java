package com.github.artsiomch.jdcr.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaDocTokenType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.javadoc.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JdcrPsiTreeUtils {

  /**
   * Check if PsiDocToken inside {@link JdcrStringUtils#CODE_TAGS} -> do not interpreting the text
   * as HTML markup
   */
  public static boolean isInsideCodeOrLiteralTag(@NotNull PsiDocToken psiDocToken) {
    if (psiDocToken.getParent() instanceof PsiInlineDocTag) {
      String parentName = ((PsiInlineDocTag) psiDocToken.getParent()).getName();
      return JdcrStringUtils.CODE_TAGS.contains(parentName);
    }
    return false;
  }

  /** Check if {@code element} is JavaDoc element */
  public static boolean isJavaDocElement(@NotNull PsiElement element) {
    return (getRootDocComment(element) != null);
  }

  @Nullable
  private static PsiDocComment getRootDocComment(@NotNull PsiElement element) {
    PsiElement parent = element.getParent();
    while (parent != null && !(parent instanceof PsiFile)) {
      if (parent instanceof PsiDocComment) return (PsiDocComment) parent;
      parent = parent.getParent();
    }
    return null;
  }

  /** Check if {@code element} inside group of folded (collapsed) regions */
  public static boolean isFolded(@NotNull PsiElement element) {
    Document document = element.getContainingFile().getViewProvider().getDocument();
    if (document == null)
      throw new java.lang.RuntimeException("Document for " + element.toString() + " is NULL.");
    Editor[] editors = EditorFactory.getInstance().getEditors(document);
    for (Editor editor : editors) {
      for (FoldRegion foldRegion : editor.getFoldingModel().getAllFoldRegions()) {
        FoldingGroup foldingGroup = foldRegion.getGroup();
        PsiDocComment rootDocComment = getRootDocComment(element);
        if (foldingGroup != null
            && rootDocComment != null
            && foldingGroup.toString().equals("JDCR " + rootDocComment.getTokenType().toString())) {
          return !foldRegion.isExpanded();
        }
      }
    }
    return true;
  }

  /**
   * Look inside {@code range} in JavaDoc {@code element} for line breaks
   *
   * @param element javadoc PsiElement
   * @param range inside <tt>element</tt> to check
   * @return List of TextRanges <b>relative</b> to {@code element} between line breaks inside {@code
   *     range}
   */
  // todo: find better alternative to LinkedList
  public static LinkedList<TextRange> excludeLineBreaks(
      @NotNull PsiElement element, @NotNull TextRange range) {
    LinkedList<TextRange> result = new LinkedList<>();
    int prevLineBreak = range.getStartOffset();
    // todo: fetch only children inside the range.
    for (PsiElement child : element.getChildren()) {
      if (range.intersectsStrict(getTextRangeInParent(child))) {
        if (child instanceof PsiWhiteSpace
            && child.getNextSibling() != null
            && child.getNextSibling().getNode().getElementType()
                == JavaDocTokenType.DOC_COMMENT_LEADING_ASTERISKS) {
          // new line PsiWhiteSpace element found
          PsiElement ws = child;
          PsiElement la = ws.getNextSibling();
          if (ws.getStartOffsetInParent() > prevLineBreak) {
            result.add(new TextRange(prevLineBreak, ws.getStartOffsetInParent()));
          }
          prevLineBreak = la.getStartOffsetInParent() + la.getTextLength() /*length of '*'s */;
        } else if (child instanceof PsiDocTag) {
          // break nested elements
          LinkedList<TextRange> subElementRanges = new LinkedList<>();
          for (TextRange nestedRange :
              excludeLineBreaks(child, new TextRange(0, child.getTextLength()))) {
            subElementRanges.add(nestedRange.shiftRight(child.getStartOffsetInParent()));
          }
          if (!subElementRanges.isEmpty()) {
            int subElementRangesStart = subElementRanges.getFirst().getStartOffset();
            if (prevLineBreak < subElementRangesStart) { // ...<b>FOR_THAT_PART{@link test1}...
              subElementRanges.addFirst(new TextRange(prevLineBreak, subElementRangesStart));
            }
            result.addAll(subElementRanges);
            prevLineBreak = subElementRanges.getLast().getEndOffset();
          }
        }
      }
    }
    if (prevLineBreak < range.getEndOffset()) {
      result.add(new TextRange(prevLineBreak, range.getEndOffset()));
    }
    return removeLeadingSpace(element, result);
  }

  // don't include ' ' at the begging of line (after leading asterisks) if any.
  private static LinkedList<TextRange> removeLeadingSpace(
      @NotNull PsiElement element, @NotNull LinkedList<TextRange> ranges) {
    LinkedList<TextRange> result = new LinkedList<>();
    for (TextRange range : ranges) {
      if (range.substring(element.getText()).charAt(0) == ' '
          && element.getText().charAt(range.getStartOffset() - 1) == '*') {
        if (range.getLength() > 1) { // hack to avoid 0 length TextRange
          result.add(new TextRange(range.getStartOffset() + 1, range.getEndOffset()));
        }
      } else result.add(range);
    }
    return result;
  }

  private static final LinkedList<TextRange> EMPTY_LIST = new LinkedList<>();

  /**
   * Check element for incomplete HTML Tag <b>end</b> (lonely `{@code >}`) and look behind for
   * incomplete HTML Tag <b>start</b> (lonely `{@code <}`) by parsing previous Siblings.
   *
   * @param element element to check
   * @return {@link TextRange}s (<i>relatively</i> to {@code element.getParent()} element) of full
   *     multiline HTML Tag, excluding service elements (leading asterisks, etc).
   */
  public static LinkedList<TextRange> getMultiLineTagRangesInParent(@NotNull PsiElement element) {
    TextRange incompleteHtmlTagEnd = JdcrStringUtils.getIncompleteHtmlTagEnd(element.getText());
    if (incompleteHtmlTagEnd != null) {
      LinkedList<TextRange> foundRangesInParent = new LinkedList<>();
      foundRangesInParent.add(incompleteHtmlTagEnd.shiftRight(element.getStartOffsetInParent()));

      // Look behind for tag start.
      PsiElement prevSibling = element.getPrevSibling();
      while (prevSibling != null) {
        if (prevSibling.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA
            || prevSibling instanceof PsiInlineDocTag) {
          TextRange incompleteHtmlTagStart =
              JdcrStringUtils.getIncompleteHtmlTagStart(prevSibling.getText());
          if (incompleteHtmlTagStart == null) {
            foundRangesInParent.addFirst(getTextRangeInParent(prevSibling));
          } else {
            foundRangesInParent.addFirst(
                incompleteHtmlTagStart.shiftRight(prevSibling.getStartOffsetInParent()));
            return removeLeadingSpace(element.getParent(), foundRangesInParent);
          }
        }
        prevSibling = prevSibling.getPrevSibling();
      }
    }
    return EMPTY_LIST;
  }

  /**
   * @return text range of element relative to its parent
   * @see PsiElement#getTextRangeInParent() - make it avaliable before 2018.3
   */
  @NotNull
  private static TextRange getTextRangeInParent(@NotNull PsiElement element) {
    return TextRange.from(element.getStartOffsetInParent(), element.getTextLength());
  }

  /**
   * Recursive (depth first) search for all elements of given {@code class}.
   *
   * <p>Don't use it. Use {@link com.intellij.psi.util.PsiTreeUtil#findChildrenOfType(PsiElement,
   * Class)}.
   *
   * @param element a PSI element to start search from.
   * @param clazz element type to search for.
   * @param <T> type to cast found elements to.
   * @return {@code List<T>} of all found elements, or empty {@code List<T>} if nothing found.
   */
  @NotNull
  public static <T extends PsiElement> List<T> myFindChildrenOfType(
      @Nullable PsiElement element, @NotNull Class<? extends T> clazz) {
    List<T> result = new ArrayList<>();
    if (element != null) {
      doFindChildrenOfType(element, clazz, result);
    }
    return result;
  }

  private static <T extends PsiElement> void doFindChildrenOfType(
      @NotNull PsiElement element, @NotNull Class<? extends T> clazz, @NotNull List<T> result) {
    for (PsiElement child : element.getChildren()) {
      if (clazz.isInstance(child)) {
        result.add(clazz.cast(child));
      }
      doFindChildrenOfType(child, clazz, result);
    }
  }
}
