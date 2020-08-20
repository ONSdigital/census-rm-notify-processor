package uk.gov.ons.census.notifyprocessor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.census.notifyprocessor.cache.UacQidCache;
import uk.gov.ons.census.notifyprocessor.model.*;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;

public class FulfilmentRequestServiceTest {

  private static final String TEST_SOURCE = "TestSource";
  private static final String TEST_CHANNEL = "TestChannel";
  private static final UUID TEST_TRANSACTION_ID = UUID.randomUUID();

  @Test
  public void testProcessMessage() {
    // Given
    EasyRandom easyRandom = new EasyRandom();
    UacQidCache uacQidCache = mock(UacQidCache.class);
    UacQid uacQid = easyRandom.nextObject(UacQid.class);
    uacQid.setUac("aaaabbbbccccdddd");
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    when(uacQidCache.getUacQidPair(anyInt())).thenReturn(uacQid);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            uacQidCache, templateMapper, rabbitTemplate, "testExchange", "testOtherExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");
    event.getEvent().setTransactionId(TEST_TRANSACTION_ID);
    event.getEvent().setSource(TEST_SOURCE);
    event.getEvent().setChannel(TEST_CHANNEL);

    // When
    underTest.processMessage(event);

    // Then
    verify(uacQidCache).getUacQidPair(eq(1));

    ArgumentCaptor<ResponseManagementEvent> rmEventArgCaptor =
        ArgumentCaptor.forClass(ResponseManagementEvent.class);
    verify(rabbitTemplate)
        .convertAndSend(eq("testOtherExchange"), eq(""), rmEventArgCaptor.capture());
    ResponseManagementEvent rmEvent = rmEventArgCaptor.getValue();
    assertThat(rmEvent.getEvent().getType()).isEqualTo(EventType.RM_UAC_CREATED);
    assertThat(rmEvent.getEvent().getTransactionId()).isEqualTo(TEST_TRANSACTION_ID);
    assertThat(rmEvent.getEvent().getSource()).isEqualTo(TEST_SOURCE);
    assertThat(rmEvent.getEvent().getChannel()).isEqualTo(TEST_CHANNEL);

    assertThat(rmEvent.getPayload().getUacQidCreated().getQid()).isEqualTo(uacQid.getQid());
    assertThat(rmEvent.getPayload().getUacQidCreated().getUac()).isEqualTo(uacQid.getUac());
    assertThat(rmEvent.getPayload().getUacQidCreated().getCaseId())
        .isEqualTo(event.getPayload().getFulfilmentRequest().getCaseId());

    ArgumentCaptor<EnrichedFulfilmentRequest> enrichedFulfilmentRequestArgumentCaptor =
        ArgumentCaptor.forClass(EnrichedFulfilmentRequest.class);
    verify(rabbitTemplate)
        .convertAndSend(
            eq("testExchange"), eq(""), enrichedFulfilmentRequestArgumentCaptor.capture());
    EnrichedFulfilmentRequest actualEnrichedFulfilmentRequest =
        enrichedFulfilmentRequestArgumentCaptor.getValue();
    assertThat(actualEnrichedFulfilmentRequest.getUac()).isEqualTo("AAAA BBBB CCCC DDDD");
  }

  @Test
  public void testProcessIndividualRequestMessage() {
    // Given
    EasyRandom easyRandom = new EasyRandom();
    UacQidCache uacQidCache = mock(UacQidCache.class);
    UacQid uacQid = easyRandom.nextObject(UacQid.class);
    uacQid.setUac("aaaabbbbccccdddd");
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    when(uacQidCache.getUacQidPair(anyInt())).thenReturn(uacQid);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            uacQidCache, templateMapper, rabbitTemplate, "testExchange", "testOtherExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACIT1");

    // When
    underTest.processMessage(event);

    // Then
    ArgumentCaptor<ResponseManagementEvent> rmEventArgCaptor =
        ArgumentCaptor.forClass(ResponseManagementEvent.class);
    verify(rabbitTemplate)
        .convertAndSend(eq("testOtherExchange"), eq(""), rmEventArgCaptor.capture());
    ResponseManagementEvent rmEvent = rmEventArgCaptor.getValue();
    assertThat(rmEvent.getEvent().getType()).isEqualTo(EventType.RM_UAC_CREATED);
    assertThat(rmEvent.getPayload().getUacQidCreated().getQid()).isEqualTo(uacQid.getQid());
    assertThat(rmEvent.getPayload().getUacQidCreated().getUac()).isEqualTo(uacQid.getUac());
    assertThat(rmEvent.getPayload().getUacQidCreated().getCaseId())
        .isEqualTo(event.getPayload().getFulfilmentRequest().getIndividualCaseId());
    verify(uacQidCache).getUacQidPair(eq(1));
  }

  @Test
  public void testProcessCeSpgIndividualRequestMessage() {
    // Given
    EasyRandom easyRandom = new EasyRandom();
    UacQidCache uacQidCache = mock(UacQidCache.class);
    UacQid uacQid = easyRandom.nextObject(UacQid.class);
    uacQid.setUac("aaaabbbbccccdddd");
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    when(uacQidCache.getUacQidPair(anyInt())).thenReturn(uacQid);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            uacQidCache, templateMapper, rabbitTemplate, "testExchange", "testOtherExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACIT1");
    event.getPayload().getFulfilmentRequest().setIndividualCaseId(null);

    // When
    underTest.processMessage(event);

    // Then
    ArgumentCaptor<ResponseManagementEvent> rmEventArgCaptor =
        ArgumentCaptor.forClass(ResponseManagementEvent.class);
    verify(rabbitTemplate)
        .convertAndSend(eq("testOtherExchange"), eq(""), rmEventArgCaptor.capture());
    ResponseManagementEvent rmEvent = rmEventArgCaptor.getValue();
    assertThat(rmEvent.getEvent().getType()).isEqualTo(EventType.RM_UAC_CREATED);
    assertThat(rmEvent.getPayload().getUacQidCreated().getQid()).isEqualTo(uacQid.getQid());
    assertThat(rmEvent.getPayload().getUacQidCreated().getUac()).isEqualTo(uacQid.getUac());
    assertThat(rmEvent.getPayload().getUacQidCreated().getCaseId())
        .isNotEqualTo(event.getPayload().getFulfilmentRequest().getIndividualCaseId());
    assertThat(rmEvent.getPayload().getUacQidCreated().getCaseId())
        .isEqualTo(event.getPayload().getFulfilmentRequest().getCaseId());
    verify(uacQidCache).getUacQidPair(eq(1));
  }

  @Test(expected = RuntimeException.class)
  public void testProcessMessageCaseNotFound() {
    // Given
    EasyRandom easyRandom = new EasyRandom();
    UacQidCache uacQidCache = mock(UacQidCache.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    when(templateMapper.getTemplate(anyString())).thenReturn(new Tuple(1, "testTemplate"));
    when(uacQidCache.getUacQidPair(anyInt())).thenThrow(RuntimeException.class);
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            uacQidCache, templateMapper, rabbitTemplate, "testExchange", "testOtherExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    // When
    underTest.processMessage(event);

    // Then
    // Never reaches this point because of KABOOM, KABLAMMO, KAPOW!!
  }

  @Test
  public void testProcessNonUacFulfilmentCode() {
    // Given
    EasyRandom easyRandom = new EasyRandom();
    UacQidCache uacQidCache = mock(UacQidCache.class);
    UacQid uacQid = easyRandom.nextObject(UacQid.class);
    uacQid.setUac("aaaabbbbccccdddd");
    TemplateMapper templateMapper = mock(TemplateMapper.class);
    RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    FulfilmentRequestService underTest =
        new FulfilmentRequestService(
            uacQidCache, templateMapper, rabbitTemplate, "testExchange", "testOtherExchange");

    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("Wibble");

    // When
    underTest.processMessage(event);

    // Then
    verifyNoInteractions(uacQidCache);
  }
}
