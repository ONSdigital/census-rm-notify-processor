package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class ResponseManagementEvent {
  private Event event;
  private Payload payload;
}
