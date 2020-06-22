package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class UacQidCreated {
  private String uac;
  private String qid;
  private String caseId;
}
