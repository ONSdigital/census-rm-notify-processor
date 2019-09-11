package uk.gov.ons.census.notifyprocessor.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

@Service
public class EnrichedFulfilmentRequestService {

  private String senderId;

  private final NotificationClientApi notificationClient;

  public EnrichedFulfilmentRequestService(
      NotificationClientApi notificationClient, @Value("${notify.senderId}") String senderId) {
    this.notificationClient = notificationClient;
    this.senderId = senderId;
  }

  public void processMessage(EnrichedFulfilmentRequest fulfilmentRequest) {
    try {
      notificationClient.sendSms(
          fulfilmentRequest.getTemplateId(),
          fulfilmentRequest.getMobileNumber(),
          Map.of("uac", fulfilmentRequest.getUac()),
          UUID.randomUUID().toString(),
          senderId);
    } catch (NotificationClientException e) {
      throw new RuntimeException(e);
    }
  }
}
