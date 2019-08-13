package uk.gov.ons.census.notifyprocessor.messaging;

import java.util.Collections;
import java.util.Map;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendSmsResponse;

@MessageEndpoint
public class FulfilmentRequestReceiver {
  private final NotificationClientApi notificationClient;

  public FulfilmentRequestReceiver(NotificationClientApi notificationClient) {
    this.notificationClient = notificationClient;
  }

  @Transactional
  @ServiceActivator(inputChannel = "fulfilmentInputChannel")
  public void receiveMessage(ResponseManagementEvent fulfilmentEvent) {

    try {
      SendSmsResponse response =
          notificationClient.sendSms("abc123", fulfilmentEvent.getPayload().getFulfilmentRequest().getContact().getTelNo(),
              Map.of("uac", "xxx"), "zzz");
    } catch (NotificationClientException e) {
      throw new RuntimeException(e);
    }

  }
}
