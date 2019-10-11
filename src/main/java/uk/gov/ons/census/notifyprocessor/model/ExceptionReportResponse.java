package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class ExceptionReportResponse {
  private boolean peek = false;
  private boolean logIt = false;
  private boolean skipIt = false;
}
