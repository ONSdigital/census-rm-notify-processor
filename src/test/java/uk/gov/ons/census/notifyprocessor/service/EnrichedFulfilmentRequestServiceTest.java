package uk.gov.ons.census.notifyprocessor.service;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

public class EnrichedFulfilmentRequestServiceTest {
  @Test
  public void testProcessMessage() throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    EnrichedFulfilmentRequestService underTest =
        new EnrichedFulfilmentRequestService(notificationClientApi, "testSenderId");

    EnrichedFulfilmentRequest enrichedFulfilmentRequest =
        easyRandom.nextObject(EnrichedFulfilmentRequest.class);

    underTest.processMessage(enrichedFulfilmentRequest);

    Map<String, String> testMap = Map.of("uac", enrichedFulfilmentRequest.getUac());
    verify(notificationClientApi)
        .sendSms(
            eq(enrichedFulfilmentRequest.getTemplateId()),
            eq(enrichedFulfilmentRequest.getMobileNumber()),
            eq(testMap),
            anyString(),
            eq("testSenderId"));
  }

  @Test(expected = RuntimeException.class)
  public void testProcessMessageNotifyApiServiceFails() throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    EnrichedFulfilmentRequestService underTest =
        new EnrichedFulfilmentRequestService(notificationClientApi, "testSenderId");
    when(notificationClientApi.sendSms(
            anyString(), anyString(), anyMap(), anyString(), anyString()))
        .thenThrow(NotificationClientException.class);
    EnrichedFulfilmentRequest enrichedFulfilmentRequest =
        easyRandom.nextObject(EnrichedFulfilmentRequest.class);

    underTest.processMessage(enrichedFulfilmentRequest);
  }

  @Test
  public void testProcessMessageNotifyApiServiceFailsBadPhoneNumber()
      throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    EnrichedFulfilmentRequestService underTest =
        new EnrichedFulfilmentRequestService(notificationClientApi, "testSenderId");
    NotificationClientException notificationClientException =
        new NotificationClientException(
            "Status code: 400 {\"errors\":[{\"error\":\"ValidationError\","
                + "\"message\":\"phone_number Not a valid country prefix\"}],\"status_code\":400}");

    when(notificationClientApi.sendSms(
            anyString(), anyString(), anyMap(), anyString(), anyString()))
        .thenThrow(notificationClientException);
    EnrichedFulfilmentRequest enrichedFulfilmentRequest =
        easyRandom.nextObject(EnrichedFulfilmentRequest.class);

    underTest.processMessage(enrichedFulfilmentRequest);
  }

  @Test
  public void testProcessMessageNotifyApiServiceFailsNonUkPhoneNumber()
      throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    EnrichedFulfilmentRequestService underTest =
        new EnrichedFulfilmentRequestService(notificationClientApi, "testSenderId");
    NotificationClientException notificationClientException =
        new NotificationClientException(
            "Status code: 400 {\"errors\":[{\"error\":\"BadRequestError\","
                + "\"message\":\"Cannot send to international mobile numbers\"}],\"status_code\":400}");

    when(notificationClientApi.sendSms(
            anyString(), anyString(), anyMap(), anyString(), anyString()))
        .thenThrow(notificationClientException);
    EnrichedFulfilmentRequest enrichedFulfilmentRequest =
        easyRandom.nextObject(EnrichedFulfilmentRequest.class);

    underTest.processMessage(enrichedFulfilmentRequest);
  }
}
