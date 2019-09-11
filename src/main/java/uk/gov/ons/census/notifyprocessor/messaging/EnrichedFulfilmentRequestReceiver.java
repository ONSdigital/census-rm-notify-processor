package uk.gov.ons.census.notifyprocessor.messaging;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.ons.census.notifyprocessor.service.EnrichedFulfilmentRequestService;

@MessageEndpoint
public class EnrichedFulfilmentRequestReceiver {
  private final EnrichedFulfilmentRequestService enrichedFulfilmentRequestService;

  public EnrichedFulfilmentRequestReceiver(
      EnrichedFulfilmentRequestService enrichedFulfilmentRequestService) {
    this.enrichedFulfilmentRequestService = enrichedFulfilmentRequestService;
  }

  @Transactional
  @ServiceActivator(inputChannel = "enrichedFulfilmentInputChannel")
  public void receiveMessage(EnrichedFulfilmentRequest enrichedFulfilmentRequest) {
    enrichedFulfilmentRequestService.processMessage(enrichedFulfilmentRequest);
  }
}
