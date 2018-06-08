package com.github.artsiomch.utils;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDCRStringUtils {
  private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
  private static final Pattern HTML_ESC_CHAR = Pattern.compile("&[^;]+;");

  /**
   * Parse given text to find HTML tags
   * @param text given text
   * @return list of TextRange of HTML tags inside text
   */
  @NotNull
  public static List<TextRange> getCombinedHtmlTags(String text) {
    return getCombinedElementsInText(text, HTML_TAG);
  }

  /**
   * Parse given text to find HTML escaped chars, such as "&nbsp"
   * @param text given text
   * @return list of TextRange of HTML escaped chars inside text
   */
  @NotNull
  public static List<TextRange> getCombinedHtmlEscapedChars(String text) {
    return getCombinedElementsInText(text, HTML_ESC_CHAR);
  }

  @NotNull
  private static List<TextRange> getCombinedElementsInText(String text, Pattern pattern) {
    Stack<TextRange> result = new Stack<>();
    Matcher matcher = pattern.matcher(text);
    int start;
    while (matcher.find()) {
      start = ( !(result.empty()) && result.peek().getEndOffset() == matcher.start() )
              ? result.pop().getStartOffset()
              : matcher.start();
      result.push( new TextRange( start, matcher.end()));
    }
    return result;
  }
}
