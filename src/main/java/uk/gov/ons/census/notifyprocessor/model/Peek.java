package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class Peek {
  private String messageHash;
  private byte[] messagePayload;
}
