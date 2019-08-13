package uk.gov.ons.census.notifyprocessor.model;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class EventDTO {
  private EventType type;
  private String source;
  private String channel;
  private String dateTime;
  private String transactionId;
}
