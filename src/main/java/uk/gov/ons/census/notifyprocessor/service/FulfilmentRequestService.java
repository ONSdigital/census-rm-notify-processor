package uk.gov.ons.census.notifyprocessor.service;

import java.util.Map;
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
    Tuple tuple =
        templateMapper.getTemplate(
            fulfilmentEvent.getPayload().getFulfilmentRequest().getFulfilmentCode());
    if (tuple == null) {
      return;
    }

    UacQidDTO uacqid =
        caseClient.getUacQid(
            fulfilmentEvent.getPayload().getFulfilmentRequest().getCaseId(),
            tuple.getQuestionnaireType());

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
