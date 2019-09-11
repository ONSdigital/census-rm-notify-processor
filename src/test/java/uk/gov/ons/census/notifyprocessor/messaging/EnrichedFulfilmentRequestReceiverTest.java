package uk.gov.ons.census.notifyprocessor.messaging;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.ons.census.notifyprocessor.service.EnrichedFulfilmentRequestService;

public class EnrichedFulfilmentRequestReceiverTest {
  @Test
  public void testReceiveMessage() {
    EnrichedFulfilmentRequestService enrichedFulfilmentRequestService =
        mock(EnrichedFulfilmentRequestService.class);
    EnrichedFulfilmentRequestReceiver underTest =
        new EnrichedFulfilmentRequestReceiver(enrichedFulfilmentRequestService);

    EnrichedFulfilmentRequest enrichedFulfilmentRequest = new EnrichedFulfilmentRequest();
    underTest.receiveMessage(enrichedFulfilmentRequest);

    verify(enrichedFulfilmentRequestService).processMessage(eq(enrichedFulfilmentRequest));
  }
}
