package uk.gov.ons.census.notifyprocessor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
public class PayloadDTO {
  @JsonInclude(Include.NON_NULL)
  private FulfilmentRequestDTO fulfilmentRequest;
}
