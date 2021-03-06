package uk.gov.ons.census.notifyprocessor.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class NotifyApiResponse {
  private UUID id;
  private UUID reference;
  private Content content;
  private Template template;

  @Data
  public class Content {
    public String body;
    public String from_number;
  }

  @Data
  public class Template {
    public UUID id;
    public int version;
    public String uri;
  }
}
