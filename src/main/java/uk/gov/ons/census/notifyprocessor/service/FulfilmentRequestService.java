package uk.gov.ons.census.notifyprocessor.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.notifyprocessor.client.CaseClient;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.model.UacQidDTO;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;

@Service
public class FulfilmentRequestService {

  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_ENGLAND = "UACIT1";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_ENGLISH = "UACIT2";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_WELSH = "UACIT2W";
  private static final String INDIVIDUAL_QUESTIONNAIRE_REQUEST_NORTHERN_IRELAND = "UACIT4";
  private static final Set<String> individualResponseRequestCodes =
      new HashSet<>(
          Arrays.asList(
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_ENGLAND,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_ENGLISH,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_WELSH,
              INDIVIDUAL_QUESTIONNAIRE_REQUEST_NORTHERN_IRELAND));

  private final CaseClient caseClient;

  private final TemplateMapper templateMapper;

  private final RabbitTemplate rabbitTemplate;

  private final String enrichedFulfilmentExchange;

  public FulfilmentRequestService(
      CaseClient caseClient,
      TemplateMapper templateMapper,
      RabbitTemplate rabbitTemplate,
      @Value("${queueconfig.enriched-fulfilment-exchange}") String enrichedFulfilmentExchange) {
    this.caseClient = caseClient;
    this.templateMapper = templateMapper;
    this.rabbitTemplate = rabbitTemplate;
    this.enrichedFulfilmentExchange = enrichedFulfilmentExchange;
  }

  public void processMessage(ResponseManagementEvent fulfilmentEvent) {
    String fulfilmentCode = fulfilmentEvent.getPayload().getFulfilmentRequest().getFulfilmentCode();
    Tuple tuple = templateMapper.getTemplate(fulfilmentCode);
    if (tuple == null) {
      return;
    }

    String caseId = fulfilmentEvent.getPayload().getFulfilmentRequest().getCaseId();

    if (individualResponseRequestCodes.contains(fulfilmentCode)) {
      caseId = fulfilmentEvent.getPayload().getFulfilmentRequest().getIndividualCaseId();
    }

    UacQidDTO uacqid = caseClient.getUacQid(caseId, tuple.getQuestionnaireType());
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
}
