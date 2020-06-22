package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class Payload {
  private FulfilmentRequest fulfilmentRequest;
  private UacQidCreated uacQidCreated;
}
