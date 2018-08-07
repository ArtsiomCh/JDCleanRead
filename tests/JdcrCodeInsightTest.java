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

  public void testFolding() {
//    myFixture.configureByFiles("FoldingTestData.java");
    myFixture.testFolding(getTestDataPath() + "/FoldingTestData.java");
  }
}
