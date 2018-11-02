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
   * Check if PsiDocToken inside @code or @literal tag -> do not interpreting the text as HTML
   * markup
   */
  public static boolean isInsideCodeOrLiteralTag(@NotNull PsiDocToken psiDocToken) {
    if (psiDocToken.getParent() instanceof PsiInlineDocTag) {
      String parentName = ((PsiInlineDocTag) psiDocToken.getParent()).getName();
      return parentName.equals("code") || parentName.equals("literal");
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
   * @param element
   * @param range
   * @return List of TextRanges relative to {@code element} between line breaks inside {@code range}
   */
  public static List<TextRange> excludeLineBreaks(
      @NotNull PsiElement element, @NotNull TextRange range) {
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
    if (prevLineBreak < range.getEndOffset()) {
      result.add(new TextRange(prevLineBreak, range.getEndOffset()));
    }
    return result;
  }

  private static final LinkedList<TextRange> EMPTY_LIST = new LinkedList<>();

  /**
   * Check element for incomplete HTML Tag <b>end</b> (lonely `{@code >}`) and look behind for
   * incomplete HTML Tag <b>start</b> (lonely `{@code <}`) by parsing previous Siblings.
   *
   * @param psiDocToken element to check
   * @return {@link TextRange}s (<i>relatively</i> to {@code psiDocToken.getParent()} element) of
   *     full multiline HTML Tag, excluding service elements (leading asterisks, etc).
   */
  public static LinkedList<TextRange> getMultiLineTagRangesInParent(
      @NotNull PsiElement psiDocToken) {
    TextRange incompleteHtmlTagEnd = JdcrStringUtils.getIncompleteHtmlTagEnd(psiDocToken.getText());
    if (incompleteHtmlTagEnd != null) {
      LinkedList<TextRange> rangesToFold = new LinkedList<>();
      rangesToFold.add(incompleteHtmlTagEnd.shiftRight(psiDocToken.getStartOffsetInParent()));

      // Look behind for tag start.
      PsiElement prevSibling = psiDocToken.getPrevSibling();
      while (prevSibling != null) {
        if (prevSibling.getNode().getElementType() == JavaDocTokenType.DOC_COMMENT_DATA
            || prevSibling instanceof PsiInlineDocTag) {
          TextRange incompleteHtmlTagStart =
              JdcrStringUtils.getIncompleteHtmlTagStart(prevSibling.getText());
          if (incompleteHtmlTagStart == null) {
            rangesToFold.addFirst(prevSibling.getTextRangeInParent());
          } else {
            rangesToFold.addFirst(
                incompleteHtmlTagStart.shiftRight(prevSibling.getStartOffsetInParent()));
            return rangesToFold;
          }
        }
        prevSibling = prevSibling.getPrevSibling();
      }
    }
    return EMPTY_LIST;
  }

  /**
   * Recursive (depth first) search for all elements of given {@code class}. Don't use it. Use
   * {@link com.intellij.psi.util.PsiTreeUtil#findChildrenOfType(PsiElement, Class)}.
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
