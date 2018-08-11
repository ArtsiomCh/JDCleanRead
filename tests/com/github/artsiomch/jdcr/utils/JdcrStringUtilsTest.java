package com.github.artsiomch.jdcr.utils;

import com.intellij.openapi.util.TextRange;
import java.util.Arrays;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

public class JdcrStringUtilsTest {

  @Test
  public void getCombinedHtmlTagsTest() {
    List<TextRange> combinedHtmlTags;
    String text;

    text = "no tags test";
    combinedHtmlTags = JdcrStringUtils.getCombinedHtmlTags(text);
    assertTrue(text, combinedHtmlTags.isEmpty());

    text = "<b> one tag test";
    combinedHtmlTags = JdcrStringUtils.getCombinedHtmlTags(text);
    assertEquals(text, combinedHtmlTags.size(), 1);
    assertEquals(text, combinedHtmlTags.get(0).getStartOffset(), 0);
    assertEquals(text, combinedHtmlTags.get(0).getEndOffset(), 3);

    text = "<b><i> combined tags test";
    combinedHtmlTags = JdcrStringUtils.getCombinedHtmlTags(text);
    assertEquals(text, combinedHtmlTags.size(), 1);
    assertEquals(text, combinedHtmlTags.get(0).getStartOffset(), 0);
    assertEquals(text, combinedHtmlTags.get(0).getEndOffset(), 6);
  }

  @Test
  public void getCombinedHtmlEscapedCharsTest() {
    List<TextRange> combinedHtmlEscapedChars;
    String text;

    text = "no HTML escaped chars test";
    combinedHtmlEscapedChars = JdcrStringUtils.getCombinedHtmlEscapedChars(text);
    assertTrue(text, combinedHtmlEscapedChars.isEmpty());

    text = "&amp; one HTML escaped char test";
    combinedHtmlEscapedChars = JdcrStringUtils.getCombinedHtmlEscapedChars(text);
    assertEquals(text, combinedHtmlEscapedChars.size(), 1);
    assertEquals(text, combinedHtmlEscapedChars.get(0).getStartOffset(), 0);
    assertEquals(text, combinedHtmlEscapedChars.get(0).getEndOffset(), 5);

    text = "&lt;&lt; combined HTML escaped chars test";
    combinedHtmlEscapedChars = JdcrStringUtils.getCombinedHtmlEscapedChars(text);
    assertEquals(text, combinedHtmlEscapedChars.size(), 1);
    assertEquals(text, combinedHtmlEscapedChars.get(0).getStartOffset(), 0);
    assertEquals(text, combinedHtmlEscapedChars.get(0).getEndOffset(), 8);
  }

  @Test
  public void getTextRangesForHtmlTagsTest() {
    List<TextRange> HtmlTags;
    List<Tag> tagsToFind = Arrays.asList(new Tag("<b>", "</b>"), new Tag("<i>", "</i>"));
    String text;

    text = "no tags test";
    HtmlTags = JdcrStringUtils.getTextRangesForHtmlTags(text, tagsToFind);
    assertTrue(text, HtmlTags.isEmpty());

    text = "<b>one</b> tag test";
    HtmlTags = JdcrStringUtils.getTextRangesForHtmlTags(text, tagsToFind);
    assertEquals(text, HtmlTags.size(), 1);
    assertEquals(text, HtmlTags.get(0).getStartOffset(), 3);
    assertEquals(text, HtmlTags.get(0).getEndOffset(), 6);

    text = "<b><i>combined</b></i> tags test";
    HtmlTags = JdcrStringUtils.getTextRangesForHtmlTags(text, tagsToFind);
    assertEquals(text, 1, HtmlTags.size());
    assertEquals(text, 6, HtmlTags.get(0).getStartOffset());
    assertEquals(text, 14, HtmlTags.get(0).getEndOffset());
  }
}