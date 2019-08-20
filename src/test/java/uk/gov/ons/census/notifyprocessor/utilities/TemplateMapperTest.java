package uk.gov.ons.census.notifyprocessor.utilities;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.*;

import org.junit.Test;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;

public class TemplateMapperTest {

  @Test
  public void testGetTemplate() {

    TemplateMapper underTest =
        new TemplateMapper(
            "TemplateA", "TemplateB", "TemplateC", "TemplateD", "TemplateE", "TemplateF");

    testTemplate(underTest, "UACHHT1", HOUSEHOLD_ENGLAND, "TemplateA");
    testTemplate(underTest, "UACHHT2", HOUSEHOLD_WALES, "TemplateB");
    testTemplate(underTest, "UACHHT2W", HOUSEHOLD_WALES, "TemplateB");
    testTemplate(underTest, "UACHHT4", HOUSEHOLD_NI, "TemplateC");
    testTemplate(underTest, "UACIT1", INDIVIDUAL_RESPONSE_ENGLAND, "TemplateD");
    testTemplate(underTest, "UACIT2", INDIVIDUAL_RESPONSE_WALES, "TemplateE");
    testTemplate(underTest, "UACIT2W", INDIVIDUAL_RESPONSE_WALES, "TemplateE");
    testTemplate(underTest, "UACIT4", INDIVIDUAL_RESPONSE_NI, "TemplateF");
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
