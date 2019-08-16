package uk.gov.ons.census.notifyprocessor.utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {

  private final String HH_E;
  private final String HH_W;
  private final String HH_NI;
  private final String IR_E;
  private final String IR_W;
  private final String IR_NI;

  public TemplateMapper(
      @Value("${notify.templateIdReplacementHHE}") String HH_E,
      @Value("${notify.templateIdReplacementHHW}") String HH_W,
      @Value("${notify.templateIdReplacementHHNI}") String HH_NI,
      @Value("${notify.templateIdIndividualResponseE}") String IR_E,
      @Value("${notify.templateIdIndividualResponseW}") String IR_W,
      @Value("${notify.templateIdIndividualResponseNI}") String IR_NI) {
    this.HH_E = HH_E;
    this.HH_W = HH_W;
    this.HH_NI = HH_NI;
    this.IR_E = IR_E;
    this.IR_W = IR_W;
    this.IR_NI = IR_NI;
  }

  public Tuple getTemplate(String fulfilmentCode) {
    Tuple result = null;
    switch (fulfilmentCode) {
      case "UACHHT1":
        result = new Tuple(1, HH_E);
        break;
      case "UACHHT2":
      case "UACHHT2W":
        result = new Tuple(2, HH_W);
        break;
      case "UACHHT4":
        result = new Tuple(4, HH_NI);
        break;
      case "UACIT1":
        result = new Tuple(21, IR_E);
        break;
      case "UACIT2":
      case "UACIT2W":
        result = new Tuple(22, IR_W);
        break;
      case "UACIT4":
        result = new Tuple(24, IR_NI);
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
