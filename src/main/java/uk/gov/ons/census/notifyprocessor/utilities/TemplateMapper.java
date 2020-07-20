package uk.gov.ons.census.notifyprocessor.utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {
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
  private final String templateEnglishHouseHold;
  private final String templateWelshHouseHold;
  private final String templateWelshAndEnglishHouseHold;
  private final String templateNorthernIrelandHouseHold;
  private final String templateEnglishIndividualResponse;
  private final String templateWelshIndividualResponse;
  private final String templateWelshAndEnglishIndividualResponse;
  private final String templateNorthernIrelandIndividualResponse;
  private final String templateEnglishCe;
  private final String templateWelshCe;
  private final String templateWelshAndEnglishCe;

  public TemplateMapper(
      @Value("${notify.templateEnglishHouseHold}") String templateEnglishHouseHold,
      @Value("${notify.templateWelshHouseHold}") String templateWelshHouseHold,
      @Value("${notify.templateWelshAndEnglishHouseHold}") String templateWelshAndEnglishHouseHold,
      @Value("${notify.templateNorthernIrelandHouseHold}") String templateNorthernIrelandHouseHold,
      @Value("${notify.templateEnglishIndividualResponse}") String templateEnglishIndividualResponse,
      @Value("${notify.templateWelshIndividualResponse}") String templateWelshIndividualResponse,
      @Value("${notify.templateWelshAndEnglishIndividualResponse}") String templateWelshAndEnglishIndividualResponse,
      @Value("${notify.templateNorthernIrelandIndividualResponse}") String templateNorthernIrelandIndividualResponse,
      @Value("${notify.templateEnglishCe}") String templateEnglishCe,
      @Value("${notify.templateWelshCe}") String templateWelshCe,
      @Value("${notify.templateWelshAndEnglishCe}") String templateWelshAndEnglishCe) {
    this.templateEnglishHouseHold = templateEnglishHouseHold;
    this.templateWelshHouseHold = templateWelshHouseHold;
    this.templateWelshAndEnglishHouseHold = templateWelshAndEnglishHouseHold;
    this.templateNorthernIrelandHouseHold = templateNorthernIrelandHouseHold;
    this.templateEnglishIndividualResponse = templateEnglishIndividualResponse;
    this.templateWelshIndividualResponse = templateWelshIndividualResponse;
    this.templateWelshAndEnglishIndividualResponse = templateWelshAndEnglishIndividualResponse;
    this.templateNorthernIrelandIndividualResponse = templateNorthernIrelandIndividualResponse;
    this.templateEnglishCe = templateEnglishCe;
    this.templateWelshCe = templateWelshCe;
    this.templateWelshAndEnglishCe = templateWelshAndEnglishCe;
  }

  public Tuple getTemplate(String fulfilmentCode) {
    Tuple result = null;
    switch (fulfilmentCode) {
      case "UACHHT1":
        result = new Tuple(HOUSEHOLD_ENGLAND, templateEnglishHouseHold);
        break;
      case "UACHHT2":
        result = new Tuple(HOUSEHOLD_WALES, templateWelshAndEnglishHouseHold);
        break;
      case "UACHHT2W":
        result = new Tuple(HOUSEHOLD_WALES_IN_WELSH, templateWelshHouseHold);
        break;
      case "UACHHT4":
        result = new Tuple(HOUSEHOLD_NI, templateNorthernIrelandHouseHold);
        break;
      case "UACIT1":
        result = new Tuple(INDIVIDUAL_RESPONSE_ENGLAND, templateEnglishIndividualResponse);
        break;
      case "UACIT2":
        result = new Tuple(INDIVIDUAL_RESPONSE_WALES, templateWelshAndEnglishIndividualResponse);
        break;
      case "UACIT2W":
        result = new Tuple(INDIVIDUAL_RESPONSE_WALES_IN_WELSH, templateWelshIndividualResponse);
        break;
      case "UACIT4":
        result = new Tuple(INDIVIDUAL_RESPONSE_NI, templateNorthernIrelandIndividualResponse);
        break;
      case "UACCET1":
        result = new Tuple(CE_ENGLAND, templateEnglishCe);
        break;
      case "UACCET2":
        result = new Tuple(CE_WALES, templateWelshAndEnglishCe);
        break;
      case "UACCET2W":
        result = new Tuple(CE_WALES_IN_WELSH, templateWelshCe);
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
