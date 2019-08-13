package uk.gov.ons.census.notifyprocessor.messaging;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;
import uk.gov.ons.census.notifyprocessor.service.FulfilmentRequestService;

public class FulfilmentRequestReceiverTest {

  @Test
  public void testReceiveMessage() {
    FulfilmentRequestService fulfilmentRequestService = mock(FulfilmentRequestService.class);
    FulfilmentRequestReceiver underTest = new FulfilmentRequestReceiver(fulfilmentRequestService);

    ResponseManagementEvent responseManagementEvent = new ResponseManagementEvent();
    underTest.receiveMessage(responseManagementEvent);

    verify(fulfilmentRequestService).processMessage(eq(responseManagementEvent));
  }
}
