package uk.gov.ons.census.notifyprocessor.utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {
  private static final int HOUSEHOLD_ENGLAND = 1;
  private static final int HOUSEHOLD_WALES_IN_ENGLISH = 2;
  private static final int HOUSEHOLD_WALES_IN_WELSH = 3;
  private static final int HOUSEHOLD_NI = 4;
  private static final int INDIVIDUAL_RESPONSE_ENGLAND = 21;
  private static final int INDIVIDUAL_RESPONSE_WALES_IN_ENGLISH = 22;
  private static final int INDIVIDUAL_RESPONSE_WALES_IN_WELSH = 23;
  private static final int INDIVIDUAL_RESPONSE_NI = 24;
  private final String templateEnglish;
  private final String templateWalesInEnglish;
  private final String templateWalesInWelsh;
  private final String templateNorthernIreland;

  public TemplateMapper(
      @Value("${notify.templateEnglish}") String templateEnglish,
      @Value("${notify.templateWelsh}") String templateWelsh,
      @Value("${notify.templateWelshAndEnglish}") String templateWelshAndEnglish,
      @Value("${notify.templateNorthernIreland}") String templateNorthernIreland) {
    this.templateEnglish = templateEnglish;
    this.templateWalesInWelsh = templateWelsh;
    this.templateWalesInEnglish = templateWelshAndEnglish;
    this.templateNorthernIreland = templateNorthernIreland;
  }

  public Tuple getTemplate(String fulfilmentCode) {
    Tuple result = null;
    switch (fulfilmentCode) {
      case "UACHHT1":
        result = new Tuple(HOUSEHOLD_ENGLAND, templateEnglish);
        break;
      case "UACHHT2":
        result = new Tuple(HOUSEHOLD_WALES_IN_ENGLISH, templateWalesInEnglish);
        break;
      case "UACHHT2W":
        result = new Tuple(HOUSEHOLD_WALES_IN_WELSH, templateWalesInWelsh);
        break;
      case "UACHHT4":
        result = new Tuple(HOUSEHOLD_NI, templateNorthernIreland);
        break;
      case "UACIT1":
        result = new Tuple(INDIVIDUAL_RESPONSE_ENGLAND, templateEnglish);
        break;
      case "UACIT2":
        result = new Tuple(INDIVIDUAL_RESPONSE_WALES_IN_ENGLISH, templateWalesInEnglish);
        break;
      case "UACIT2W":
        result = new Tuple(INDIVIDUAL_RESPONSE_WALES_IN_WELSH, templateWalesInWelsh);
        break;
      case "UACIT4":
        result = new Tuple(INDIVIDUAL_RESPONSE_NI, templateNorthernIreland);
        break;

      default:
        break;
    }

    return result;
  }

  @Data
  @AllArgsConstructor
  public static class Tuple {
    public int questionnaireType;
    public String templateId;
  }
}
