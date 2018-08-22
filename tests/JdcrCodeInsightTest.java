import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class JdcrCodeInsightTest extends LightCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return "testData";
  }

  public void testAnnotatorTagsHighlighting() {
    myFixture.configureByFiles("AnnotatorTestData.java");
    myFixture.checkHighlighting(false, true, false, false);
  }

  public void testFoldingHtmlTags() {
    myFixture.testFolding(getTestDataPath() + "/FoldingHtmlTagsTestData.java");
  }

  public void testFoldingCodeTag() {
    myFixture.testFolding(getTestDataPath() + "/FoldingCodeTagTestData.java");
  }

  public void testFoldingCodeTagWithGenerics() {
    myFixture.testFolding(getTestDataPath() + "/FoldingCodeTagWithGenericsTestData.java");
  }

  public void testFoldingLiteralTag() {
    myFixture.testFolding(getTestDataPath() + "/FoldingLiteralTagTestData.java");
  }

  public void testFoldingLinkTag() {
    myFixture.testFolding(getTestDataPath() + "/FoldingLinkTagTestData.java");
  }

  public void testFoldingLinkMultilineAfterTag() {
    myFixture.testFolding(getTestDataPath() + "/FoldingLinkMultilineAfterTagTestData.java");
  }

  public void testFoldingLinkMultilineBeforeTag() {
    myFixture.testFolding(getTestDataPath() + "/FoldingLinkMultilineBeforeTagTestData.java");
  }

  public void testFoldingEscapedChars() {
    myFixture.testFolding(getTestDataPath() + "/FoldingEscapedCharsTestData.java");
  }
}
