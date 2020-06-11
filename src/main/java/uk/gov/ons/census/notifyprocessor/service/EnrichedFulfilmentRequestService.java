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
  public static final String BAD_REQUEST_ERROR = "BadRequestError";
  public static final String VALIDATION_ERROR = "ValidationError";
  private static final String CANNOT_SEND_INTERNATIONAL_MESSAGE =
      "Cannot send to international mobile numbers";
  private static final String PHONE_NUMBER_VALIDATION_FAIL_MESSAGE = "phone_number ";
  public static final String PHONE_NUMBER_FAILED_VALIDATION =
      "Phone number failed Gov Notify validation (SHOULD be caught upstream but wasn't)";
  private final String senderId;

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
    } catch (NotificationClientException notificationClientException) {
      handleNotificationClientException(notificationClientException);
    }
  }

  private void handleNotificationClientException(
      NotificationClientException notificationClientException) {
    String exceptionMessage = notificationClientException.getMessage();

    if (exceptionMessage.contains(BAD_REQUEST_ERROR)
        && exceptionMessage.contains(CANNOT_SEND_INTERNATIONAL_MESSAGE)) {
      log.with("problem", CANNOT_SEND_INTERNATIONAL_MESSAGE).warn(PHONE_NUMBER_FAILED_VALIDATION);
    } else if (exceptionMessage.contains(VALIDATION_ERROR)
        && exceptionMessage.contains(PHONE_NUMBER_VALIDATION_FAIL_MESSAGE)) {
      String phoneNumberProblem =
          exceptionMessage.substring(
              exceptionMessage.indexOf(PHONE_NUMBER_VALIDATION_FAIL_MESSAGE));
      phoneNumberProblem =
          phoneNumberProblem.substring(
              PHONE_NUMBER_VALIDATION_FAIL_MESSAGE.length(), phoneNumberProblem.indexOf("\""));

      log.with("problem", phoneNumberProblem).warn(PHONE_NUMBER_FAILED_VALIDATION);
    } else {
      throw new RuntimeException(
          String.format(
              "Gov Notify sendSms NotificationClientException error with status code %d and "
                  + "message: %s",
              notificationClientException.getHttpResult(),
              notificationClientException.getMessage()),
          notificationClientException);
    }
  }
}
