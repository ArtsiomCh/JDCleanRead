package com.github.artsiomch.jdcr;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.java.JavaSyntaxHighlighterFactory;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.util.containers.ContainerUtil;
import java.awt.Font;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;

public class JdcrColorSettingsPage implements ColorSettingsPage {
  public static final TextAttributesKey CODE_TAG =
      TextAttributesKey.createTextAttributesKey(
          "code tag", DefaultLanguageHighlighterColors.DOC_COMMENT_MARKUP);
  public static final TextAttributesKey LINK_TAG =
      TextAttributesKey.createTextAttributesKey(
          "link tag", DefaultLanguageHighlighterColors.CLASS_NAME);
  public static final TextAttributesKey HTML_LINK_TAG =
      TextAttributesKey.createTextAttributesKey(
          "html link tag", DefaultLanguageHighlighterColors.NUMBER);

  public static final TextAttributesKey BOLD_FONT =
      TextAttributesKey.createTextAttributesKey("bold font", createBoldTextAttributes());
  public static final TextAttributesKey ITALIC_FONT =
      TextAttributesKey.createTextAttributesKey("italic font", createBoldTextAttributes());

  private static TextAttributes createBoldTextAttributes() {
    TextAttributes textAttributes =
        DefaultLanguageHighlighterColors.DOC_COMMENT.getDefaultAttributes().clone();
    /** Set or revert FontType. See {@link Font} */
    textAttributes.setFontType(textAttributes.getFontType() ^ Font.BOLD);
    return textAttributes;
  }

  private static final AttributesDescriptor[] DESCRIPTORS =
      new AttributesDescriptor[] {
        new AttributesDescriptor("Tag value of: <code> | <tt> | @code | <pre>", CODE_TAG),
        new AttributesDescriptor("Tag value of html link: <a href=\"...\">...</a>", HTML_LINK_TAG),
        new AttributesDescriptor("Tag value of: @link", LINK_TAG),
        new AttributesDescriptor("Tag value of: <b>", BOLD_FONT),
        new AttributesDescriptor("Tag value of: <i> | <em>", ITALIC_FONT)
      };

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return JavaSyntaxHighlighterFactory.getSyntaxHighlighter(JavaLanguage.INSTANCE, null, null);
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "/**\n"
        + " * To convert any <tt><_code>object</_code></tt> of {@code <_code>Object</_code>} class to <code><_code>String</_code></code> use \n"
        + " * {@link java.lang.Object#<_link>toString()</_link> toString()} method.\n"
        + " * html link <a href=\"http://www.jetbrains.org\"><_a>JetBrains</_a></a>.\n"
        + " * <b><_b>bold text</_b></b> and <i><_i>italic text</_i></i>.\n"
        + " */";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ContainerUtil.newHashMap(
        Arrays.asList("_code", "_link", "_a", "_b", "_i"),
        Arrays.asList(CODE_TAG, LINK_TAG, HTML_LINK_TAG, BOLD_FONT, ITALIC_FONT));
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
