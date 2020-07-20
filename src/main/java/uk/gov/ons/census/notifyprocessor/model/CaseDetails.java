package uk.gov.ons.census.notifyprocessor.model;

import java.util.UUID;
import lombok.Data;

@Data
public class CaseDetails {

  private UUID caseId;

  private String questionnaireType;
}
