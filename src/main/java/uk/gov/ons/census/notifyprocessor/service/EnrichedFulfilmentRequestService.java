package uk.gov.ons.census.notifyprocessor.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

@Service
public class EnrichedFulfilmentRequestService {
  private static final Logger log = LoggerFactory.getLogger(EnrichedFulfilmentRequestService.class);

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
      log.with("exception", e).error("Gov Notify sendSms error");
      throw new RuntimeException("Could not send SMS", e);
    }
  }
}
