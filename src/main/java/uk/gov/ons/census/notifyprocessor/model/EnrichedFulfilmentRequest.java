package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class EnrichedFulfilmentRequest {
  private String templateId;
  private String mobileNumber;
  private String uac;
}
