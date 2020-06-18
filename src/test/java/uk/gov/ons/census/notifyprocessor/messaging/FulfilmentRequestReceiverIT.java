package uk.gov.ons.census.notifyprocessor.messaging;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
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
import uk.gov.ons.census.notifyprocessor.model.Event;
import uk.gov.ons.census.notifyprocessor.model.EventType;
import uk.gov.ons.census.notifyprocessor.model.FulfilmentRequest;
import uk.gov.ons.census.notifyprocessor.model.Payload;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.model.UacQid;
import uk.gov.ons.census.notifyprocessor.utilities.RabbitQueueHelper;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
public class FulfilmentRequestReceiverIT {
  private static final String CASE_EVENT_EXCHANGE = "events";
  private static final String MULTIPLE_QIDS_URL = "/multiple_qids";
  public static final String SMS_NOTIFY_API_URL = "/v2/notifications/sms";
  private static final String CASE_UAC_QID_CREATED_QUEUE = "case.uac-qid-created";

  @Autowired private RabbitQueueHelper rabbitQueueHelper;

  @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().port(8089));

  @Value("${queueconfig.fulfilment-request-inbound-queue}")
  private String fulfilmentInboundQueue;

  @Value("${queueconfig.fulfilment-routing-key}")
  private String caseProcessorFulfilmentRoutingKeyCase;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  @Transactional
  public void setUp() {
    rabbitQueueHelper.purgeQueue(fulfilmentInboundQueue);
    rabbitQueueHelper.purgeQueue(CASE_UAC_QID_CREATED_QUEUE);
  }

  @Test
  public void testHappyPath() throws JsonProcessingException, InterruptedException {
    BlockingQueue<String> uacQidCreatedQueue = rabbitQueueHelper.listen(CASE_UAC_QID_CREATED_QUEUE);

    EasyRandom easyRandom = new EasyRandom();

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();
    responseManagementEvent.setEvent(new Event());
    responseManagementEvent.setPayload(new Payload());
    responseManagementEvent.getPayload().setFulfilmentRequest(new FulfilmentRequest());
    responseManagementEvent.getPayload().getFulfilmentRequest().setCaseId("test caseId");
    responseManagementEvent.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");
    responseManagementEvent.getPayload().getFulfilmentRequest().setContact(new Contact());
    responseManagementEvent.getPayload().getFulfilmentRequest().getContact().setTelNo("012345");

    UacQid uacQid = stubCreateUacQid(1);

    NotifyApiResponse notifyApiResponse = easyRandom.nextObject(NotifyApiResponse.class);
    String notifyApiResponseJson = objectMapper.writeValueAsString(notifyApiResponse);

    stubFor(
        post(urlEqualTo(SMS_NOTIFY_API_URL))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(notifyApiResponseJson)));

    rabbitQueueHelper.sendMessage(
        CASE_EVENT_EXCHANGE, caseProcessorFulfilmentRoutingKeyCase, responseManagementEvent);

    // Wait for the message to be processed
    Thread.sleep(1000);

    // Now check the stubs to ensure they got called
    verify(1, postRequestedFor(urlEqualTo("/v2/notifications/sms")));

    String actualUacQidCreateMessage = uacQidCreatedQueue.poll(20, TimeUnit.SECONDS);
    assertThat(actualUacQidCreateMessage).isNotNull();
    ResponseManagementEvent actualRmUacQidCreateEvent =
        objectMapper.readValue(actualUacQidCreateMessage, ResponseManagementEvent.class);
    assertThat(actualRmUacQidCreateEvent.getEvent().getType()).isEqualTo(EventType.RM_UAC_CREATED);
    assertThat(actualRmUacQidCreateEvent.getPayload().getUacQidCreated().getCaseId())
        .isEqualTo(
            responseManagementEvent.getPayload().getFulfilmentRequest().getCaseId().toString());
    assertThat(actualRmUacQidCreateEvent.getPayload().getUacQidCreated().getQid())
        .isEqualTo(uacQid.getQid());
    assertThat(actualRmUacQidCreateEvent.getPayload().getUacQidCreated().getUac())
        .isEqualTo(uacQid.getUac());
  }

  private UacQid stubCreateUacQid(int questionnaireType) throws JsonProcessingException {
    EasyRandom easyRandom = new EasyRandom();
    UacQid uacQid = easyRandom.nextObject(UacQid.class);
    uacQid.setQid(String.format("%02d", questionnaireType) + uacQid.getQid());
    UacQid[] uacQidList = new UacQid[1];
    uacQidList[0] = uacQid;
    String uacQidDtoJson = objectMapper.writeValueAsString(uacQidList);
    stubFor(
        get(urlPathEqualTo(MULTIPLE_QIDS_URL))
            .withQueryParam("questionnaireType", equalTo(Integer.toString(questionnaireType)))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", "application/json")
                    .withBody(uacQidDtoJson)));
    return uacQid;
  }
}
