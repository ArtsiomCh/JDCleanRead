package com.github.artsiomch.jdcr.utils;

import com.intellij.openapi.util.TextRange;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

public class JdcrStringUtilsTest {

  @Test
  public void getHtmlTagsTest() {
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

    text = "Html comment <!-- not_a_tag -->";
    htmlTags = JdcrStringUtils.getHtmlTags(text);
    assertTrue(text, htmlTags.isEmpty());

    // fixme: text = "Html comment <!-- <not_a_tag> -->";
    htmlTags = JdcrStringUtils.getHtmlTags(text);
    assertTrue(text, htmlTags.isEmpty());
  }

  @Test
  public void getIncompleteHtmlTagTest() {
    TextRange incompleteHtmlTag;
    String text;

    text = "no incomplete <tag> test";
    incompleteHtmlTag = JdcrStringUtils.getIncompleteHtmlTagStart(text);
    assertNull(text, incompleteHtmlTag);
    incompleteHtmlTag = JdcrStringUtils.getIncompleteHtmlTagEnd(text);
    assertNull(text, incompleteHtmlTag);

    text = "incomplete <tag start test";
    incompleteHtmlTag = JdcrStringUtils.getIncompleteHtmlTagStart(text);
    assertNotNull(text, incompleteHtmlTag);
    assertEquals(text, 11, incompleteHtmlTag.getStartOffset());
    assertEquals(text, 26, incompleteHtmlTag.getEndOffset());

    text = "full <tag> and incomplete <tag start test";
    incompleteHtmlTag = JdcrStringUtils.getIncompleteHtmlTagStart(text);
    assertNotNull(text, incompleteHtmlTag);
    assertEquals(text, 26, incompleteHtmlTag.getStartOffset());
    assertEquals(text, 41, incompleteHtmlTag.getEndOffset());

    text = "incomplete tag> end test";
    incompleteHtmlTag = JdcrStringUtils.getIncompleteHtmlTagEnd(text);
    assertNotNull(text, incompleteHtmlTag);
    assertEquals(text, 0, incompleteHtmlTag.getStartOffset());
    assertEquals(text, 15, incompleteHtmlTag.getEndOffset());

    text = "incomplete tag> end full <tag> test";
    incompleteHtmlTag = JdcrStringUtils.getIncompleteHtmlTagEnd(text);
    assertNotNull(text, incompleteHtmlTag);
    assertEquals(text, 0, incompleteHtmlTag.getStartOffset());
    assertEquals(text, 15, incompleteHtmlTag.getEndOffset());
  }

    @Test
  public void getHtmlEscapedCharsTest() {
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
  public void getValuesOfTagTest() {
    List<TextRange> htmlTagValues;
    Tag tag1 = new Tag("<tag1>", "</tag1>");
    Tag tag2 = new Tag("<tag2>", "</tag2>");
    String text;

    text = "no tags test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertTrue(text, htmlTagValues.isEmpty());

    text = "<tag1>one</tag1> tag test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, htmlTagValues.size(), 1);
    assertEquals(text, htmlTagValues.get(0).getStartOffset(), 6);
    assertEquals(text, htmlTagValues.get(0).getEndOffset(), 9);

    text = "<tag1>one</tag1> tag happens <tag1>twice</tag1> withing the row test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 2, htmlTagValues.size());
    assertEquals(text, 6, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 9, htmlTagValues.get(0).getEndOffset());
    assertEquals(text, 35, htmlTagValues.get(1).getStartOffset());
    assertEquals(text, 40, htmlTagValues.get(1).getEndOffset());

    text = "<tag1>first</tag1> and <tag2>second</tag2> tags withing the row test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 6, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 11, htmlTagValues.get(0).getEndOffset());
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag2);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 29, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 35, htmlTagValues.get(0).getEndOffset());

    text = "<tag1><tag2>combined</tag2></tag1> tags test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 6, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 27, htmlTagValues.get(0).getEndOffset());
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag2);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 12, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 20, htmlTagValues.get(0).getEndOffset());

    text = "possible multiline tag value</tag1> end test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 0, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 28, htmlTagValues.get(0).getEndOffset());

    text = "possible multiline tag value <tag1>start test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 35, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 45, htmlTagValues.get(0).getEndOffset());

    text = "<tag1>one</tag1> tag and possible multiline tag value <tag1>start test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 2, htmlTagValues.size());
    assertEquals(text, 6, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 9, htmlTagValues.get(0).getEndOffset());
    assertEquals(text, 60, htmlTagValues.get(1).getStartOffset());
    assertEquals(text, 70, htmlTagValues.get(1).getEndOffset());

    text = "possible multiline tag value</tag1> end and <tag1>another</tag1> tag occurrence test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 2, htmlTagValues.size());
    assertEquals(text, 0, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 28, htmlTagValues.get(0).getEndOffset());
    assertEquals(text, 50, htmlTagValues.get(1).getStartOffset());
    assertEquals(text, 57, htmlTagValues.get(1).getEndOffset());

    text = "possible multiline tag value</tag1> end and start<tag1> test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 2, htmlTagValues.size());
    assertEquals(text, 0, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 28, htmlTagValues.get(0).getEndOffset());
    assertEquals(text, 55, htmlTagValues.get(1).getStartOffset());
    assertEquals(text, 60, htmlTagValues.get(1).getEndOffset());

    text = "possible multiline tag value</tag1> end and <tag1>another</tag1> tag occurrence and start<tag1> test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, tag1);
    assertEquals(text, 3, htmlTagValues.size());
    assertEquals(text, 0, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 28, htmlTagValues.get(0).getEndOffset());
    assertEquals(text, 50, htmlTagValues.get(1).getStartOffset());
    assertEquals(text, 57, htmlTagValues.get(1).getEndOffset());
    assertEquals(text, 95, htmlTagValues.get(2).getStartOffset());
    assertEquals(text, 100, htmlTagValues.get(2).getEndOffset());

    Tag htmlTagRef = new Tag("<a", "</a>");
    text = "<a href=\"www\">HtmlLink</a> tags test";
    htmlTagValues = JdcrStringUtils.getValuesOfTag(text, htmlTagRef);
    assertEquals(text, 1, htmlTagValues.size());
    assertEquals(text, 14, htmlTagValues.get(0).getStartOffset());
    assertEquals(text, 22, htmlTagValues.get(0).getEndOffset());
  }
}
