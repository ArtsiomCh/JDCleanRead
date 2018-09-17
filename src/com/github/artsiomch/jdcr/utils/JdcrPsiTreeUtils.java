package com.github.artsiomch.jdcr.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.*;
import org.jetbrains.annotations.NotNull;

public class JdcrPsiTreeUtils {

  /**
   * Check if PsiDocToken inside @code or @literal tag -> do not interpreting the text as HTML
   * markup
   */
  public static boolean isInsideCodeOrLiteralTag(PsiDocToken psiDocToken) {
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

  public static PsiDocComment getRootDocComment(@NotNull PsiElement element) {
    PsiElement parent = element.getParent();
    while (parent != null) {
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
}
