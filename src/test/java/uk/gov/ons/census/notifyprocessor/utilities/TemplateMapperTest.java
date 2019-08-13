package uk.gov.ons.census.notifyprocessor.utilities;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;

public class TemplateMapperTest {

  @Test
  public void testGetTemplate() {

    TemplateMapper underTest = new TemplateMapper("TemplateA", "TemplateB");

    testTemplate(underTest, "UACHHT1", 1, "TemplateA");
    testTemplate(underTest, "UACHHT2", 2, "TemplateA");
    testTemplate(underTest, "UACHHT2W", 2, "TemplateA");
    testTemplate(underTest, "UACHHT4", 4, "TemplateA");
    testTemplate(underTest, "UACIT1", 21, "TemplateB");
    testTemplate(underTest, "UACIT2", 22, "TemplateB");
    testTemplate(underTest, "UACIT2W", 22, "TemplateB");
    testTemplate(underTest, "UACIT4", 24, "TemplateB");
    assertThat(underTest.getTemplate("Wibble")).isNull();
  }

  private void testTemplate(
      TemplateMapper underTest,
      String fulfilmentCode,
      int expectedQuestionnaireType,
      String expectedTemplate) {
    Tuple actualResult = underTest.getTemplate(fulfilmentCode);

    assertThat(actualResult.getTemplateId()).isEqualTo(expectedTemplate);
    assertThat(actualResult.getQuestionnaireType()).isEqualTo(expectedQuestionnaireType);
  }
}
