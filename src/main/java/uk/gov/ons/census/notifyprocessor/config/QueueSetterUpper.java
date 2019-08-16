package uk.gov.ons.census.notifyprocessor.config;

import static org.springframework.amqp.core.Binding.DestinationType.QUEUE;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueSetterUpper {

  @Value("${queueconfig.case-event-exchange}")
  private String caseEventExchange;

  @Value("${queueconfig.fulfilment-routing-key}")
  private String caseProcessorFulfilmentRoutingKeyCase;

  @Value("${queueconfig.fulfilment-request-inbound-queue}")
  private String fulfilmentInboundQueue;

  @Bean
  public Exchange myTopicExchange() {
    return new TopicExchange(caseEventExchange, true, false);
  }

  @Bean
  public Binding bindingFulfilmentCase() {
    return new Binding(
        fulfilmentInboundQueue,
        QUEUE,
        caseEventExchange,
        caseProcessorFulfilmentRoutingKeyCase,
        null);
  }

  @Bean
  public Queue fulfilmentInboundQueue() {
    return new Queue(fulfilmentInboundQueue, true);
  }
}
