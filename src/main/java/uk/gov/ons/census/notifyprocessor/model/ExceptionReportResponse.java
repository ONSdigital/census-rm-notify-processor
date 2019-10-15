package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class ExceptionReportResponse {
  private boolean peek;
  private boolean logIt;
  private boolean skipIt;
}
