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
  private final String UACHHT1;
  private final String UACHHT2W;
  private final String UACHHT2;
  private final String UACHHT4;
  private final String UACIT1;
  private final String UACIT2W;
  private final String UACIT2;
  private final String UACIT4;
  private final String UACCET1;
  private final String UACCET2W;
  private final String UACCET2;

  public TemplateMapper(
      @Value("${notify.UACHHT1}") String UACHHT1,
      @Value("${notify.UACHHT2W}") String UACHHT2W,
      @Value("${notify.UACHHT2}") String UACHHT2,
      @Value("${notify.UACHHT4}") String UACHHT4,
      @Value("${notify.UACIT1}") String UACIT1,
      @Value("${notify.UACIT2W}") String UACIT2W,
      @Value("${notify.UACIT2}") String UACIT2,
      @Value("${notify.UACIT4}") String UACIT4,
      @Value("${notify.UACCET1}") String UACCET1,
      @Value("${notify.UACCET2W}") String UACCET2W,
      @Value("${notify.UACCET2}") String UACCET2) {
    this.UACHHT1 = UACHHT1;
    this.UACHHT2W = UACHHT2W;
    this.UACHHT2 = UACHHT2;
    this.UACHHT4 = UACHHT4;
    this.UACIT1 = UACIT1;
    this.UACIT2W = UACIT2W;
    this.UACIT2 = UACIT2;
    this.UACIT4 = UACIT4;
    this.UACCET1 = UACCET1;
    this.UACCET2W = UACCET2W;
    this.UACCET2 = UACCET2;
  }

  public Tuple getTemplate(String fulfilmentCode) {
    Tuple result = null;
    switch (fulfilmentCode) {
      case "UACHHT1":
        result = new Tuple(HOUSEHOLD_ENGLAND, UACHHT1);
        break;
      case "UACHHT2":
        result = new Tuple(HOUSEHOLD_WALES, UACHHT2);
        break;
      case "UACHHT2W":
        result = new Tuple(HOUSEHOLD_WALES_IN_WELSH, UACHHT2W);
        break;
      case "UACHHT4":
        result = new Tuple(HOUSEHOLD_NI, UACHHT4);
        break;
      case "UACIT1":
        result = new Tuple(INDIVIDUAL_RESPONSE_ENGLAND, UACIT1);
        break;
      case "UACIT2":
        result = new Tuple(INDIVIDUAL_RESPONSE_WALES, UACIT2);
        break;
      case "UACIT2W":
        result = new Tuple(INDIVIDUAL_RESPONSE_WALES_IN_WELSH, UACIT2W);
        break;
      case "UACIT4":
        result = new Tuple(INDIVIDUAL_RESPONSE_NI, UACIT4);
        break;
      case "UACCET1":
        result = new Tuple(CE_ENGLAND, UACCET1);
        break;
      case "UACCET2":
        result = new Tuple(CE_WALES, UACCET2);
        break;
      case "UACCET2W":
        result = new Tuple(CE_WALES_IN_WELSH, UACCET2W);
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
