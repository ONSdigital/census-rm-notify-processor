package uk.gov.ons.census.notifyprocessor.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.notifyprocessor.client.CaseClient;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.model.UacQidDTO;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper;
import uk.gov.ons.census.notifyprocessor.utilities.TemplateMapper.Tuple;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

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

  private boolean testMode;

  private String senderId;

  private final NotificationClientApi notificationClient;

  private final CaseClient caseClient;

  private final TemplateMapper templateMapper;

  public FulfilmentRequestService(
      NotificationClientApi notificationClient,
      CaseClient caseClient,
      @Value("${notify.testMode}") boolean testMode,
      @Value("${notify.senderId}") String senderId,
      TemplateMapper templateMapper) {
    this.notificationClient = notificationClient;
    this.caseClient = caseClient;
    this.testMode = testMode;
    this.senderId = senderId;
    this.templateMapper = templateMapper;
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

    if (!testMode) {
      try {
        notificationClient.sendSms(
            tuple.getTemplateId(),
            fulfilmentEvent.getPayload().getFulfilmentRequest().getContact().getTelNo(),
            Map.of("uac", uacqid.getUac()),
            UUID.randomUUID().toString(),
            senderId);
      } catch (NotificationClientException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
