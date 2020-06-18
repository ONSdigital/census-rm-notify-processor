package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class Event {
  private EventType type;
  private String source;
  private String channel;
  private String dateTime;
  private String transactionId;
}
