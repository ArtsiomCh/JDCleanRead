import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

public class JdcrCodeInsightTest extends LightCodeInsightFixtureTestCase {

  @Override
  protected String getTestDataPath() {
    return "testData";
  }

  public void testAnnotatorTagsHighlighting() {
    myFixture.configureByFiles("testAnnotatorTags.java");
    myFixture.checkHighlighting(false, true, false, false);
  }

}
