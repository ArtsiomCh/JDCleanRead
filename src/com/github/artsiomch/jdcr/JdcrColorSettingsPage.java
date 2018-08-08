package com.github.artsiomch.jdcr;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.JavaSyntaxHighlighterFactory;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;

public class JdcrColorSettingsPage implements ColorSettingsPage {
  public static final TextAttributesKey CODE_TAG = TextAttributesKey.createTextAttributesKey(
          "code tag", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
  public static final TextAttributesKey LINK_TAG = TextAttributesKey.createTextAttributesKey(
          "link tag", DefaultLanguageHighlighterColors.CLASS_NAME);

  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
          new AttributesDescriptor("Tag value of: <code> | <tt> | @code", CODE_TAG),
          new AttributesDescriptor("Tag value of: @link", LINK_TAG)
  };

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return JavaSyntaxHighlighterFactory.getSyntaxHighlighter( JavaLanguage.INSTANCE, null, null);
  }

  @NotNull
  @Override
  public String getDemoText() {
    return  "/**\n"+
            " * To convert any <tt><_code>object</_code></tt> of {@code <_code>Object</_code>} class to <code><_code>String</_code></code> use \n"+
            " * {@link java.lang.Object#<_link>toString()</_link> toString()} method.\n"+
            " */"
    ;
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ContainerUtil.newHashMap(
            Arrays.asList( "_code", "_link" ),
            Arrays.asList( CODE_TAG, LINK_TAG ));
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "JavaDoc Clean Read";
  }
}
