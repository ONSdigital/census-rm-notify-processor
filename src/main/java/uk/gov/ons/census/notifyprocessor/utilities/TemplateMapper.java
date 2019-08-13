package uk.gov.ons.census.notifyprocessor.utilities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TemplateMapper {

  private final String templateIdReplacementHH;
  private final String templateIdIndividualResponse;

  public TemplateMapper(
      @Value("${notify.templateIdReplacementHH}") String templateIdReplacementHH,
      @Value("${notify.templateIdIndividualResponse}") String templateIdIndividualResponse) {
    this.templateIdReplacementHH = templateIdReplacementHH;
    this.templateIdIndividualResponse = templateIdIndividualResponse;
  }

  public Tuple getTemplate(String fulfilmentCode) {
    Tuple result = null;
    switch (fulfilmentCode) {
      case "UACHHT1":
        result = new Tuple(1, templateIdReplacementHH);
        break;
      case "UACHHT2":
      case "UACHHT2W":
        result = new Tuple(2, templateIdReplacementHH);
        break;
      case "UACHHT4":
        result = new Tuple(4, templateIdReplacementHH);
        break;
      case "UACIT1":
        result = new Tuple(21, templateIdIndividualResponse);
        break;
      case "UACIT2":
      case "UACIT2W":
        result = new Tuple(22, templateIdIndividualResponse);
        break;
      case "UACIT4":
        result = new Tuple(24, templateIdIndividualResponse);
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
