package com.github.artsiomch.jdcr.utils;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JdcrStringUtils {
  private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
  private static final Pattern HTML_ESC_CHAR = Pattern.compile("&[^;]+;");

  /**
   * Parse given text to find HTML tags
   *
   * @param text given text
   * @return list of TextRange of HTML tags inside text
   */
  @NotNull
  public static List<TextRange> getCombinedHtmlTags(String text) {
    return getCombinedElementsInText(text, HTML_TAG);
  }

  @NotNull
  private static List<TextRange> getCombinedElementsInText(String text, Pattern pattern) {
    Stack<TextRange> result = new Stack<>();
    Matcher matcher = pattern.matcher(text);
    int start;
    while (matcher.find()) {
      start =
          (!(result.empty()) && result.peek().getEndOffset() == matcher.start())
              ? result.pop().getStartOffset()
              : matcher.start();
      result.push(new TextRange(start, matcher.end()));
    }
    return result;
  }

  /**
   * Parse given text to find HTML escaped chars, such as "&nbsp"
   *
   * @param text given text
   * @return list of TextRange of HTML escaped chars inside text
   */
  @NotNull
  public static List<TextRange> getHtmlEscapedChars(String text) {
    return getElementsInText(text, HTML_ESC_CHAR);
  }

  @NotNull
  private static List<TextRange> getElementsInText(String text, Pattern pattern) {
    List<TextRange> result = new ArrayList<>();
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      result.add(new TextRange(matcher.start(), matcher.end()));
    }
    return result;
  }

  private static final int EMPTY_INDEX = -2;
  private static final List<TextRange> EMPTY_ARRAY = new ArrayList<>();

  /**
   * Parse given text to find given HTML tags
   *
   * @param text text to parse
   * @param tagsToFind HTML tags to search for
   * @return list of TextRange of HTML tags inside text if any
   */
  @NotNull
  public static List<TextRange> getTextRangesForHtmlTags(@NotNull String text, @NotNull List<Tag> tagsToFind) {
    List<TextRange> foundHtmlTags = getCombinedHtmlTags(text);
    if (foundHtmlTags.isEmpty()) {
      return EMPTY_ARRAY;
    }
    List<TextRange> result = new ArrayList<>();
    int start = EMPTY_INDEX, end = EMPTY_INDEX;
    for (Tag tag : tagsToFind) {
      for (TextRange textRange : foundHtmlTags) {
        if (textRange.substring(text).contains(tag.open)) {
          start = textRange.getEndOffset();
        }
        if (textRange.substring(text).contains(tag.close)) {
          end = textRange.getStartOffset();
        }
        if (start != EMPTY_INDEX && end != EMPTY_INDEX && start < end) {
          //        assert start <= end : "Start="+start+" End="+end+" at: "+element.getText();
          TextRange newTextRange = new TextRange(start, end);
          // exclude dublicates
          if (result.stream().noneMatch(it -> it.contains(newTextRange))) {
            result.add(newTextRange);
          }
          start = EMPTY_INDEX;
          end = EMPTY_INDEX;
        }
      }
    }
    return result;
  }
}
