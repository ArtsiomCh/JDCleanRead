package com.github.artsiomch.jdcr.utils;

import com.intellij.openapi.util.TextRange;
import java.util.Arrays;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

public class JdcrStringUtilsTest {

  @Test
  public void getCombinedHtmlTagsTest() {
    List<TextRange> htmlTags;
    String text;

    text = "no tags test";
    htmlTags = JdcrStringUtils.getHtmlTags(text);
    assertTrue(text, htmlTags.isEmpty());

    text = "<b> one tag test";
    htmlTags = JdcrStringUtils.getHtmlTags(text);
    assertEquals(text, htmlTags.size(), 1);
    assertEquals(text, htmlTags.get(0).getStartOffset(), 0);
    assertEquals(text, htmlTags.get(0).getEndOffset(), 3);

    text = "<b><i> combined tags test";
    htmlTags = JdcrStringUtils.getHtmlTags(text);
    assertEquals(text, htmlTags.size(), 2);
    assertEquals(text, htmlTags.get(0).getStartOffset(), 0);
    assertEquals(text, htmlTags.get(0).getEndOffset(), 3);
    assertEquals(text, htmlTags.get(1).getStartOffset(), 3);
    assertEquals(text, htmlTags.get(1).getEndOffset(), 6);
  }

  @Test
  public void getCombinedHtmlEscapedCharsTest() {
    List<TextRange> htmlEscapedChars;
    String text;

    text = "no HTML escaped chars test";
    htmlEscapedChars = JdcrStringUtils.getHtmlEscapedChars(text);
    assertTrue(text, htmlEscapedChars.isEmpty());

    text = "&amp; one HTML escaped char test";
    htmlEscapedChars = JdcrStringUtils.getHtmlEscapedChars(text);
    assertEquals(text, htmlEscapedChars.size(), 1);
    assertEquals(text, htmlEscapedChars.get(0).getStartOffset(), 0);
    assertEquals(text, htmlEscapedChars.get(0).getEndOffset(), 5);

    text = "&lt;&lt; two HTML escaped chars test";
    htmlEscapedChars = JdcrStringUtils.getHtmlEscapedChars(text);
    assertEquals(text, 2, htmlEscapedChars.size());
    assertEquals(text, 0, htmlEscapedChars.get(0).getStartOffset());
    assertEquals(text, 4, htmlEscapedChars.get(0).getEndOffset());
    assertEquals(text, 4, htmlEscapedChars.get(1).getStartOffset());
    assertEquals(text, 8, htmlEscapedChars.get(1).getEndOffset());
  }

  @Test
  public void getTextRangesForHtmlTagsTest() {
    List<TextRange> htmlTagValues;
    List<Tag> tagsToFind = Arrays.asList(new Tag("<b>", "</b>"), new Tag("<i>", "</i>"));
    String text;

    text = "no tags test";
    htmlTagValues = JdcrStringUtils.getTextRangesForHtmlTagValues(text, tagsToFind);
    assertTrue(text, htmlTagValues.isEmpty());

    text = "<b>one</b> tag test";
    htmlTagValues = JdcrStringUtils.getTextRangesForHtmlTagValues(text, tagsToFind);
    assertEquals(text, htmlTagValues.size(), 1);
    assertEquals(text, htmlTagValues.get(0).getStartOffset(), 3);
    assertEquals(text, htmlTagValues.get(0).getEndOffset(), 6);

    text = "<b><i>combined</b></i> tags test";
    htmlTagValues = JdcrStringUtils.getTextRangesForHtmlTagValues(text, tagsToFind);
    assertEquals(text, 2, htmlTagValues.size());
    assertEquals(text, 3, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 14, htmlTagValues.get(0).getEndOffset());
    assertEquals(text, 6, htmlTagValues.get(1).getStartOffset());
    assertEquals(text, 18, htmlTagValues.get(1).getEndOffset());

    tagsToFind = Arrays.asList(new Tag("<a href=\"", "\">"), new Tag("\">", "</a>"));
    text = "<a href=\"www\">HtmlLink</a> tags test";
    htmlTagValues = JdcrStringUtils.getTextRangesForHtmlTagValues(text, tagsToFind);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 14, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 22, htmlTagValues.get(0).getEndOffset());
  }
}
