package com.github.artsiomch;

import com.github.artsiomch.utils.JDCRStringUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class JDCRFoldingBuilder implements FoldingBuilder {
  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    PsiElement root = node.getPsi();
    FoldingGroup group = FoldingGroup.newGroup("jdcr");
    List<FoldingDescriptor> descriptors = new ArrayList<>();

    class JDCRFoldingDescriptor extends FoldingDescriptor {
      private String placeholderText;

      private JDCRFoldingDescriptor(@NotNull ASTNode node,
                                    @NotNull TextRange range,
                                    @Nullable String placeholderText) {
        super(node, range, group);
        this.placeholderText = placeholderText;
      }

      @Override
      public String getPlaceholderText() {
        return placeholderText;
      }
    }

    PsiTreeUtil.findChildrenOfType( root, PsiDocToken.class).forEach( psiDocToken -> {
      JDCRStringUtils.getHtmlTags( psiDocToken.getText()).forEach( textRange ->
              descriptors.add( new JDCRFoldingDescriptor(
                      psiDocToken.getNode(),
                      textRange.shiftRight(psiDocToken.getTextOffset()),
                      "◊")
              ));
      JDCRStringUtils.getHtmlEscapedChars( psiDocToken.getText()).forEach( textRange ->
              descriptors.add( new JDCRFoldingDescriptor(
                      psiDocToken.getNode(),
                      textRange.shiftRight(psiDocToken.getTextOffset()),
                      Jsoup.parse( textRange.substring( psiDocToken.getText())).text())
              ));
    });
    return descriptors.toArray(new FoldingDescriptor[0]);
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull ASTNode node) {
    return null;
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull ASTNode node) {
    return true;
  }
}
