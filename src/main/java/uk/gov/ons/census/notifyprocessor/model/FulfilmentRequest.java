package uk.gov.ons.census.notifyprocessor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FulfilmentRequest {

  @JsonInclude(Include.NON_NULL)
  private UUID caseId;

  private String fulfilmentCode;

  @JsonInclude(Include.NON_NULL)
  private UUID individualCaseId;

  private Contact contact;
}
