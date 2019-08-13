package uk.gov.ons.census.notifyprocessor.service;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import uk.gov.ons.census.notifyprocessor.client.CaseClient;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.model.UacQidDTO;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

public class FulfilmentRequestServiceTest {

  @Test
  public void testProcessMessage() throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    CaseClient caseClient = mock(CaseClient.class);
    UacQidDTO uacQidDTO = easyRandom.nextObject(UacQidDTO.class);
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    when(caseClient.getUacQid(anyString(), anyInt())).thenReturn(uacQidDTO);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            notificationClientApi, caseClient, false, "testSenderId", templateMapper);

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    underTest.processMessage(event);
    Map<String, String> testMap = Map.of("uac", uacQidDTO.getUac());
    verify(caseClient).getUacQid(eq(event.getPayload().getFulfilmentRequest().getCaseId()), eq(1));
    verify(notificationClientApi)
        .sendSms(
            eq("testTemplate"),
            eq(event.getPayload().getFulfilmentRequest().getContact().getTelNo()),
            eq(testMap),
            anyString(),
            eq("testSenderId"));
  }

  @Test
  public void testProcessMessageTestMode() throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    CaseClient caseClient = mock(CaseClient.class);
    UacQidDTO uacQidDTO = easyRandom.nextObject(UacQidDTO.class);
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    when(caseClient.getUacQid(anyString(), anyInt())).thenReturn(uacQidDTO);

    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            notificationClientApi, caseClient, true, "testSenderId", templateMapper);

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    underTest.processMessage(event);
    verify(caseClient).getUacQid(eq(event.getPayload().getFulfilmentRequest().getCaseId()), eq(1));
    verify(notificationClientApi, never())
        .sendSms(anyString(), anyString(), anyMap(), anyString(), anyString());
  }

  @Test(expected = RuntimeException.class)
  public void testProcessMessageCaseNotFound() throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    CaseClient caseClient = mock(CaseClient.class);
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    when(caseClient.getUacQid(anyString(), anyInt())).thenThrow(RuntimeException.class);

    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            notificationClientApi, caseClient, false, "testSenderId", templateMapper);

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    underTest.processMessage(event);
  }

  @Test(expected = RuntimeException.class)
  public void testProcessMessageNotifyApiServiceFails() throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    CaseClient caseClient = mock(CaseClient.class);
    UacQidDTO uacQidDTO = easyRandom.nextObject(UacQidDTO.class);
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    when(caseClient.getUacQid(anyString(), anyInt())).thenReturn(uacQidDTO);
    when(notificationClientApi.sendSms(
            anyString(), anyString(), anyMap(), anyString(), anyString()))
        .thenThrow(NotificationClientException.class);

    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            notificationClientApi, caseClient, false, "testSenderId", templateMapper);

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    underTest.processMessage(event);
  }

  @Test
  public void testProcessNonUacFulfilmentCode() throws NotificationClientException {
    EasyRandom easyRandom = new EasyRandom();
    NotificationClientApi notificationClientApi = mock(NotificationClientApi.class);
    CaseClient caseClient = mock(CaseClient.class);
    UacQidDTO uacQidDTO = easyRandom.nextObject(UacQidDTO.class);
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    when(caseClient.getUacQid(anyString(), anyInt())).thenReturn(uacQidDTO);

    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            notificationClientApi, caseClient, true, "testSenderId", templateMapper);

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("Wibble");

    underTest.processMessage(event);
    verify(caseClient, never()).getUacQid(anyString(), anyInt());
    verify(notificationClientApi, never())
        .sendSms(anyString(), anyString(), anyMap(), anyString(), anyString());
  }
}
