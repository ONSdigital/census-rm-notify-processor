package uk.gov.ons.census.notifyprocessor.model;

import java.util.UUID;
import lombok.Data;

@Data
public class Event {
  private EventType type;
  private String source;
  private String channel;
  private String dateTime;
  private UUID transactionId;
}
