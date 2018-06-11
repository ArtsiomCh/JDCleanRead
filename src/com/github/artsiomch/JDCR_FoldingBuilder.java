package com.github.artsiomch;

import com.github.artsiomch.utils.JDCR_StringUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.NamedFoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiAnnotatedJavaCodeReferenceElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.parser.Parser;

import java.util.ArrayList;
import java.util.List;

public class JDCR_FoldingBuilder implements FoldingBuilder {

  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
    PsiElement root = node.getPsi();
    List<FoldingDescriptor> descriptors = new ArrayList<>();

    PsiTreeUtil.findChildrenOfType( root, PsiDocComment.class).forEach( psiDocComment -> {
      final FoldingGroup foldingGroup = FoldingGroup.newGroup("JDCR " + psiDocComment.getTokenType().toString());

      PsiTreeUtil.findChildrenOfType( psiDocComment, PsiDocToken.class).forEach( psiDocToken -> {
        JDCR_StringUtils.getCombinedHtmlTags( psiDocToken.getText()).forEach(textRange -> {
          String placeholderText = "";//"◊";
          if ( textRange.substring( psiDocToken.getText()).toLowerCase().contains("<li>") ) placeholderText = " - ";
          addFoldingDescriptor(psiDocToken, textRange, foldingGroup, descriptors,
                  placeholderText
          );
        });
        JDCR_StringUtils.getCombinedHtmlEscapedChars( psiDocToken.getText()).forEach(textRange ->
                addFoldingDescriptor( psiDocToken, textRange, foldingGroup, descriptors,
                        Parser.unescapeEntities( textRange.substring( psiDocToken.getText()), true)
                ));
      });

      PsiTreeUtil.findChildrenOfType( psiDocComment, PsiInlineDocTag.class).forEach( psiInlineDocTag -> {
        if (psiInlineDocTag.getName().equals("code")) {
          TextRange textRangeTagStart = new TextRange( 0, 6);
          TextRange textRangeTagEnd = new TextRange( psiInlineDocTag.getTextLength() - 1, psiInlineDocTag.getTextLength());
          addFoldingDescriptor( psiInlineDocTag, textRangeTagStart, foldingGroup, descriptors, "");
          addFoldingDescriptor( psiInlineDocTag, textRangeTagEnd, foldingGroup, descriptors, "");
        } else if (psiInlineDocTag.getName().equals("link")) {
          PsiElement psiDocLink = psiInlineDocTag.getValueElement(); // PsiTreeUtil.findChildOfType( psiInlineDocTag, PsiDocMethodOrFieldRef.class);
          if (psiDocLink==null) psiDocLink = PsiTreeUtil.findChildOfType( psiInlineDocTag, PsiAnnotatedJavaCodeReferenceElement.class);
          if (psiDocLink!=null) {
            TextRange textRangeTagStart = new TextRange( 0, 6);// psiDocLink.getTextRange().getStartOffset() - psiInlineDocTag.getTextOffset());
            TextRange textRangeTagEnd = new TextRange( //7
                    psiDocLink.getTextRange().getStartOffset() - psiInlineDocTag.getTextRange().getStartOffset() + psiDocLink.getTextLength(),
                    psiInlineDocTag.getTextLength());
            addFoldingDescriptor( psiInlineDocTag, textRangeTagStart, foldingGroup, descriptors, "");
            addFoldingDescriptor( psiInlineDocTag, textRangeTagEnd, foldingGroup, descriptors, "");
          }
        }
      });
    });

    return descriptors.toArray(new FoldingDescriptor[0]);
  }

  private void addFoldingDescriptor(PsiElement element,
                                    TextRange range,
                                    final FoldingGroup foldingGroup,
                                    List<FoldingDescriptor> descriptors,
                                    String placeholderText) {
    descriptors.add( new NamedFoldingDescriptor(
            element.getNode(),
            range.shiftRight( element.getTextRange().getStartOffset()),
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
