package uk.gov.ons.census.notifyprocessor.utilities;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;

public class TemplateMapperTest {
  private static final int HOUSEHOLD_ENGLAND = 1;
  private static final int HOUSEHOLD_WALES = 2;
  private static final int HOUSEHOLD_WALES_IN_WELSH = 3;
  private static final int HOUSEHOLD_NI = 4;
  private static final int INDIVIDUAL_RESPONSE_ENGLAND = 21;
  private static final int INDIVIDUAL_RESPONSE_WALES = 22;
  private static final int INDIVIDUAL_RESPONSE_WALES_IN_WELSH = 23;
  private static final int INDIVIDUAL_RESPONSE_NI = 24;
  private static final int CE_ENGLAND = 31;
  private static final int CE_WALES = 32;
  private static final int CE_WALES_IN_WELSH = 33;

  @Test
  public void testGetTemplate() {

    TemplateMapper underTest =
        new TemplateMapper(
            "UACHHT1",
            "UACHHT2W",
            "UACHHT2",
            "UACHHT4",
            "UACIT1",
            "UACIT2W",
            "UACIT2",
            "UACIT4",
            "UACCET1",
            "UACCET2W",
            "UACCET2",
            "UACITA1",
            "UACITA2B",
            "UACITA4");

    testTemplate(underTest, "UACHHT1", HOUSEHOLD_ENGLAND, "UACHHT1");
    testTemplate(underTest, "UACHHT2", HOUSEHOLD_WALES, "UACHHT2");
    testTemplate(underTest, "UACHHT2W", HOUSEHOLD_WALES_IN_WELSH, "UACHHT2W");
    testTemplate(underTest, "UACHHT4", HOUSEHOLD_NI, "UACHHT4");
    testTemplate(underTest, "UACIT1", INDIVIDUAL_RESPONSE_ENGLAND, "UACIT1");
    testTemplate(underTest, "UACIT2", INDIVIDUAL_RESPONSE_WALES, "UACIT2");
    testTemplate(underTest, "UACIT2W", INDIVIDUAL_RESPONSE_WALES_IN_WELSH, "UACIT2W");
    testTemplate(underTest, "UACIT4", INDIVIDUAL_RESPONSE_NI, "UACIT4");
    testTemplate(underTest, "UACCET1", CE_ENGLAND, "UACCET1");
    testTemplate(underTest, "UACCET2", CE_WALES, "UACCET2");
    testTemplate(underTest, "UACCET2W", CE_WALES_IN_WELSH, "UACCET2W");
    testTemplate(underTest, "UACITA1", INDIVIDUAL_RESPONSE_ENGLAND, "UACITA1");
    testTemplate(underTest, "UACITA2B", INDIVIDUAL_RESPONSE_WALES, "UACITA2B");
    testTemplate(underTest, "UACITA4", INDIVIDUAL_RESPONSE_NI, "UACITA4");

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
