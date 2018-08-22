package com.github.artsiomch.jdcr.utils;

import com.intellij.psi.javadoc.*;

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
}
