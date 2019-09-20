package uk.gov.ons.census.notifyprocessor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.census.notifyprocessor.client.CaseClient;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.model.UacQidDTO;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;

public class FulfilmentRequestServiceTest {

  @Test
  public void testProcessMessage() {
    EasyRandom easyRandom = new EasyRandom();
    CaseClient caseClient = mock(CaseClient.class);
    UacQidDTO uacQidDTO = easyRandom.nextObject(UacQidDTO.class);
    uacQidDTO.setUac("aaaabbbbccccdddd");
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    when(caseClient.getUacQid(anyString(), anyInt())).thenReturn(uacQidDTO);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(caseClient, templateMapper, rabbitTemplate, "testExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    underTest.processMessage(event);
    verify(caseClient).getUacQid(eq(event.getPayload().getFulfilmentRequest().getCaseId()), eq(1));
    ArgumentCaptor<EnrichedFulfilmentRequest> enrichedFulfilmentRequestArgumentCaptor =
        ArgumentCaptor.forClass(EnrichedFulfilmentRequest.class);
    verify(rabbitTemplate)
        .convertAndSend(eq("testExchange"), eq(""), enrichedFulfilmentRequestArgumentCaptor.capture());
    EnrichedFulfilmentRequest actualEnrichedFulfilmentRequest =
        enrichedFulfilmentRequestArgumentCaptor.getValue();
    assertThat(actualEnrichedFulfilmentRequest.getUac()).isEqualTo("AAAA BBBB CCCC DDDD");
  }

  @Test
  public void testProcessIndividualRequestMessage() {
    EasyRandom easyRandom = new EasyRandom();
    CaseClient caseClient = mock(CaseClient.class);
    UacQidDTO uacQidDTO = easyRandom.nextObject(UacQidDTO.class);
    uacQidDTO.setUac("aaaabbbbccccdddd");
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    when(caseClient.getUacQid(anyString(), anyInt())).thenReturn(uacQidDTO);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(caseClient, templateMapper, rabbitTemplate, "testExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACIT1");

    underTest.processMessage(event);
    Map<String, String> testMap = Map.of("uac", uacQidDTO.getUac());
    verify(caseClient)
        .getUacQid(eq(event.getPayload().getFulfilmentRequest().getIndividualCaseId()), eq(1));
  }

  @Test(expected = RuntimeException.class)
  public void testProcessMessageCaseNotFound() {
    EasyRandom easyRandom = new EasyRandom();
    CaseClient caseClient = mock(CaseClient.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    when(caseClient.getUacQid(anyString(), anyInt())).thenThrow(RuntimeException.class);
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(caseClient, templateMapper, rabbitTemplate, "testExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    underTest.processMessage(event);
  }

  @Test
  public void testProcessNonUacFulfilmentCode() {
    EasyRandom easyRandom = new EasyRandom();
    CaseClient caseClient = mock(CaseClient.class);
    UacQidDTO uacQidDTO = easyRandom.nextObject(UacQidDTO.class);
    uacQidDTO.setUac("aaaabbbbccccdddd");
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    when(caseClient.getUacQid(anyString(), anyInt())).thenReturn(uacQidDTO);
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(caseClient, templateMapper, rabbitTemplate, "testExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("Wibble");

    underTest.processMessage(event);
    verify(caseClient, never()).getUacQid(anyString(), anyInt());
  }
}
