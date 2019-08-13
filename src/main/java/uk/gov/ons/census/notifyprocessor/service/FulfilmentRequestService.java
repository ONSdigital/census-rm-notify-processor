package uk.gov.ons.census.notifyprocessor.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.notifyprocessor.client.CaseClient;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.model.UacQidDTO;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendSmsResponse;

@Service
public class FulfilmentRequestService {

  @Value("${notify.testMode}")
  private boolean testMode;

  private final NotificationClientApi notificationClient;

  private final CaseClient caseClient;



  public FulfilmentRequestService(NotificationClientApi notificationClient,
      CaseClient caseClient) {
    this.notificationClient = notificationClient;
    this.caseClient = caseClient;
  }

  public void processMessage(ResponseManagementEvent fulfilmentEvent) {
    UacQidDTO uacqid = caseClient
        .getUacQid(fulfilmentEvent.getPayload().getFulfilmentRequest().getCaseId());

    if (!testMode) {
      try {
        SendSmsResponse response =
            notificationClient.sendSms(
                "templateID",
                fulfilmentEvent.getPayload().getFulfilmentRequest().getContact().getTelNo(),
                Map.of("uac", uacqid.getUac()),
                "reference");
      } catch (NotificationClientException e) {
        throw new RuntimeException(e);
      }
    }
  }


  }
