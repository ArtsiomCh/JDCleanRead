package com.github.artsiomch;

import com.github.artsiomch.utils.JDCRStringUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public class JDCRFoldingBuilder implements FoldingBuilder {

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    PsiElement root = node.getPsi();
    List<FoldingDescriptor> descriptors = new ArrayList<>();

    PsiTreeUtil.findChildrenOfType( root, PsiDocComment.class).forEach( psiDocComment -> {
      final FoldingGroup foldingGroup = FoldingGroup.newGroup("JDCR " + psiDocComment.toString());
      PsiTreeUtil.findChildrenOfType( psiDocComment, PsiDocToken.class).forEach( psiDocToken -> {
        JDCRStringUtils.getCombinedHtmlTags( psiDocToken.getText()).forEach(textRange -> {
          String placeholderText = "◊";
          if ( textRange.substring( psiDocToken.getText()).contains("<li>") ) placeholderText = " * ";
          addFoldingDescriptor(psiDocToken, textRange, foldingGroup, descriptors,
                  placeholderText
          );
        });
        JDCRStringUtils.getCombinedHtmlEscapedChars( psiDocToken.getText()).forEach(textRange ->
                addFoldingDescriptor( psiDocToken, textRange, foldingGroup, descriptors,
                        Parser.unescapeEntities( textRange.substring( psiDocToken.getText()), true)
                ));
      });
    });
    return descriptors.toArray(new FoldingDescriptor[0]);
  }

  private void addFoldingDescriptor(PsiDocToken psiDocToken,
                                    TextRange textRange,
                                    final FoldingGroup foldingGroup,
                                    List<FoldingDescriptor> descriptors,
                                    String placeholderText) {
    descriptors.add( new NamedFoldingDescriptor(
            psiDocToken.getNode(),
            textRange.shiftRight( psiDocToken.getTextOffset()),
            foldingGroup,
            placeholderText
    ));
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
