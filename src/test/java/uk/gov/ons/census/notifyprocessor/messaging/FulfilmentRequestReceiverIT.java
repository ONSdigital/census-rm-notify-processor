package uk.gov.ons.census.notifyprocessor.messaging;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.census.notifyprocessor.dto.NotifyApiResponse;
import uk.gov.ons.census.notifyprocessor.model.Contact;
import uk.gov.ons.census.notifyprocessor.model.EventDTO;
import uk.gov.ons.census.notifyprocessor.model.FulfilmentRequestDTO;
import uk.gov.ons.census.notifyprocessor.model.PayloadDTO;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.model.UacQidDTO;
import uk.gov.ons.census.notifyprocessor.utilities.RabbitQueueHelper;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
public class FulfilmentRequestReceiverIT {

  @Autowired private RabbitQueueHelper rabbitQueueHelper;

  @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().port(8089));

  @Value("${queueconfig.case-event-exchange}")
  private String caseEventExchange;

  @Value("${queueconfig.fulfilment-request-inbound-queue}")
  private String fulfilmentInboundQueue;

  @Value("${queueconfig.fulfilment-routing-key}")
  private String caseProcessorFulfilmentRoutingKeyCase;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  @Transactional
  public void setUp() {
    rabbitQueueHelper.purgeQueue(fulfilmentInboundQueue);
  }

  @Test
  public void testHappyPath() throws JsonProcessingException, InterruptedException {
    EasyRandom easyRandom = new EasyRandom();

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();
    responseManagementEvent.setEvent(new EventDTO());
    responseManagementEvent.setPayload(new PayloadDTO());
    responseManagementEvent.getPayload().setFulfilmentRequest(new FulfilmentRequestDTO());
    responseManagementEvent.getPayload().getFulfilmentRequest().setCaseId("test caseId");
    responseManagementEvent.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");
    responseManagementEvent.getPayload().getFulfilmentRequest().setContact(new Contact());
    responseManagementEvent.getPayload().getFulfilmentRequest().getContact().setTelNo("012345");

    UacQidDTO uacQidDTO = new UacQidDTO();
    uacQidDTO.setQid("test QID");
    uacQidDTO.setUac("test UAC");

    String returnJson = objectMapper.writeValueAsString(uacQidDTO);

    NotifyApiResponse notifyApiResponse = easyRandom.nextObject(NotifyApiResponse.class);
    String notifyApiResponseJson = objectMapper.writeValueAsString(notifyApiResponse);

    stubFor(
        post(urlEqualTo("/uacqid/create"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(returnJson)));

    stubFor(
        post(urlEqualTo("/v2/notifications/sms"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(notifyApiResponseJson)));

    rabbitQueueHelper.sendMessage(
        caseEventExchange, caseProcessorFulfilmentRoutingKeyCase, responseManagementEvent);

    // Wait for the message to be processed
    Thread.sleep(1000);

    // Now check the stubs to ensure they got called
    verify(1, postRequestedFor(urlEqualTo("/uacqid/create")));
    verify(1, postRequestedFor(urlEqualTo("/v2/notifications/sms")));
  }
}
