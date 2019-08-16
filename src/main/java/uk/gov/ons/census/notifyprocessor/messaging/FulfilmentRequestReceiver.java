package uk.gov.ons.census.notifyprocessor.messaging;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.service.FulfilmentRequestService;

@MessageEndpoint
public class FulfilmentRequestReceiver {
  private final FulfilmentRequestService fulfilmentRequestService;

  public FulfilmentRequestReceiver(FulfilmentRequestService fulfilmentRequestService) {
    this.fulfilmentRequestService = fulfilmentRequestService;
  }

  @Transactional
  @ServiceActivator(inputChannel = "fulfilmentInputChannel")
  public void receiveMessage(ResponseManagementEvent fulfilmentEvent) {
    fulfilmentRequestService.processMessage(fulfilmentEvent);
  }
}
