package uk.gov.ons.census.notifyprocessor.service;

import static uk.gov.ons.census.notifyprocessor.model.EventType.RM_UAC_CREATED;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.notifyprocessor.cache.UacQidCache;
import uk.gov.ons.census.notifyprocessor.model.*;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;

@Service
public class FulfilmentRequestService {

  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_ENGLAND = "UACIT1";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_ENGLISH = "UACIT2";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_WELSH = "UACIT2W";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_NORTHERN_IRELAND = "UACIT4";

  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_VIA_EQ_ENGLAND = "UACITA1";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_VIA_EQ_WALES_ENGLISH = "UACITA2B";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_VIA_EQ_NORTHERN_IRELAND = "UACITA4";
  private static final Set<String> individualResponseRequestCodes =
      new HashSet<>(
          Arrays.asList(
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_ENGLAND,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_ENGLISH,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_WELSH,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_NORTHERN_IRELAND,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_VIA_EQ_ENGLAND,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_VIA_EQ_WALES_ENGLISH,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_VIA_EQ_NORTHERN_IRELAND));

  private final UacQidCache uacQidCache;

  private final TemplateMapper templateMapper;

  private final RabbitTemplate rabbitTemplate;

  private final String enrichedFulfilmentExchange;

  private final String uacQidCreatedExchange;

  public FulfilmentRequestService(
      UacQidCache uacQidCache,
      TemplateMapper templateMapper,
      RabbitTemplate rabbitTemplate,
      @Value("${queueconfig.enriched-fulfilment-exchange}") String enrichedFulfilmentExchange,
      @Value("${queueconfig.uac-qid-created-exchange}") String uacQidCreatedExchange) {
    this.uacQidCache = uacQidCache;
    this.templateMapper = templateMapper;
    this.rabbitTemplate = rabbitTemplate;
    this.enrichedFulfilmentExchange = enrichedFulfilmentExchange;
    this.uacQidCreatedExchange = uacQidCreatedExchange;
  }

  public void processMessage(ResponseManagementEvent fulfilmentEvent) {
    String fulfilmentCode = fulfilmentEvent.getPayload().getFulfilmentRequest().getFulfilmentCode();
    Tuple tuple = templateMapper.getTemplate(fulfilmentCode);
    if (tuple == null) {
      return;
    }

    UUID caseId = fulfilmentEvent.getPayload().getFulfilmentRequest().getCaseId();

    if (individualResponseRequestCodes.contains(fulfilmentCode)
        && fulfilmentEvent.getPayload().getFulfilmentRequest().getIndividualCaseId() != null) {
      caseId = fulfilmentEvent.getPayload().getFulfilmentRequest().getIndividualCaseId();
    }

    UacQid uacqid = getUacQidPair(tuple.getQuestionnaireType(), caseId, fulfilmentEvent.getEvent());

    EnrichedFulfilmentRequest enrichedFulfilmentRequest = new EnrichedFulfilmentRequest();
    enrichedFulfilmentRequest.setTemplateId(tuple.getTemplateId());
    enrichedFulfilmentRequest.setMobileNumber(
        fulfilmentEvent.getPayload().getFulfilmentRequest().getContact().getTelNo());
    String uac = uacqid.getUac();
    String formattedUac =
        String.format(
            "%s %s %s %s",
            uac.substring(0, 4), uac.substring(4, 8), uac.substring(8, 12), uac.substring(12, 16));
    enrichedFulfilmentRequest.setUac(formattedUac.toUpperCase());

    // Send a message to ourselves - in case Gov Notify is down
    rabbitTemplate.convertAndSend(enrichedFulfilmentExchange, "", enrichedFulfilmentRequest);
  }

  private UacQid getUacQidPair(int questionnaireType, UUID caseId, Event receivedEvent) {
    UacQid uacqid = uacQidCache.getUacQidPair(questionnaireType);

    UacQidCreated uacQidCreated = new UacQidCreated();
    uacQidCreated.setCaseId(caseId);
    uacQidCreated.setQid(uacqid.getQid());
    uacQidCreated.setUac(uacqid.getUac());

    Event event = new Event();
    event.setType(RM_UAC_CREATED);
    event.setDateTime(OffsetDateTime.now());
    event.setTransactionId(receivedEvent.getTransactionId());
    event.setChannel(receivedEvent.getChannel());
    event.setSource(receivedEvent.getSource());
    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();
    responseManagementEvent.setEvent(event);
    Payload payload = new Payload();
    payload.setUacQidCreated(uacQidCreated);
    responseManagementEvent.setPayload(payload);

    // This message to Case Processor will ensure the UAC-QID is persisted: eventual consistency
    rabbitTemplate.convertAndSend(uacQidCreatedExchange, "", responseManagementEvent);

    return uacqid;
  }
}
