package com.github.artsiomch.utils;

import com.intellij.openapi.util.TextRange;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDCRStringUtils {
  @RegExp private static final String HTML_TAG = "<[^>]+>";
  @RegExp private static final String HTML_ESC_CHAR = "&[^;]+;";

  /**
   * Parse giving text to find HTML tags
   * @param text
   * @return list of TextRange of HTML tags inside text
   */
  @NotNull
  public static List<TextRange> getHtmlTags(String text) {
    return getElementsInText(text, HTML_TAG);
  }

  /**
   * Parse giving text to find HTML escaped chars, such as "&nbsp"
   * @param text
   * @return list of TextRange of HTML escaped chars inside text
   */
  @NotNull
  public static List<TextRange> getHtmlEscapedChars(String text) {
    return getElementsInText(text, HTML_ESC_CHAR);
  }

  private static List<TextRange> getElementsInText(String text, String elementRegex) {
    List<TextRange> result = new ArrayList<>();
    Pattern pattern = Pattern.compile( elementRegex);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      result.add( new TextRange( matcher.start(), matcher.end()));
    }
    return result;
  }
}
