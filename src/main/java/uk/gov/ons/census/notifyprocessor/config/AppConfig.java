package uk.gov.ons.census.notifyprocessor.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.notifyprocessor.client.ExceptionManagerClient;
import uk.gov.ons.census.notifyprocessor.messaging.MessageErrorHandler;
import uk.gov.ons.census.notifyprocessor.model.EnrichedFulfilmentRequest;
import uk.gov.ons.census.notifyprocessor.model.ResponseManagementEvent;

@Configuration
@EnableScheduling
public class AppConfig {
  @Value("${messagelogging.logstacktraces}")
  private boolean logStackTraces;

  @Value("${queueconfig.consumers}")
  private int consumers;

  @Value("${queueconfig.fulfilment-request-inbound-queue}")
  private String fulfilmentInboundQueue;

  @Value("${queueconfig.enriched-fulfilment-queue}")
  private String enrichedFulfilmentQueue;

  @Bean
  public MessageChannel fulfilmentInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel enrichedFulfilmentInputChannel() {
    return new DirectChannel();
  }

  @Bean
  public AmqpInboundChannelAdapter fulfilmentInbound(
      @Qualifier("fulfilmentContainer") SimpleMessageListenerContainer listenerContainer,
      @Qualifier("fulfilmentInputChannel") MessageChannel channel) {
    AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
    adapter.setOutputChannel(channel);
    return adapter;
  }

  @Bean
  public AmqpInboundChannelAdapter enrichedfulfilmentInbound(
      @Qualifier("enrichedFulfilmentContainer") SimpleMessageListenerContainer listenerContainer,
      @Qualifier("enrichedFulfilmentInputChannel") MessageChannel channel) {
    AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
    adapter.setOutputChannel(channel);
    return adapter;
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter);
    rabbitTemplate.setChannelTransacted(true);
    return rabbitTemplate;
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public SimpleMessageListenerContainer fulfilmentContainer(
      ConnectionFactory connectionFactory, ExceptionManagerClient exceptionManagerClient) {
    return setupListenerContainer(
        connectionFactory,
        fulfilmentInboundQueue,
        exceptionManagerClient,
        ResponseManagementEvent.class);
  }

  @Bean
  public SimpleMessageListenerContainer enrichedFulfilmentContainer(
      ConnectionFactory connectionFactory, ExceptionManagerClient exceptionManagerClient) {
    return setupListenerContainer(
        connectionFactory,
        enrichedFulfilmentQueue,
        exceptionManagerClient,
        EnrichedFulfilmentRequest.class);
  }

  @Bean
  public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  private SimpleMessageListenerContainer setupListenerContainer(
      ConnectionFactory connectionFactory,
      String queueName,
      ExceptionManagerClient exceptionManagerClient,
      Class expectedMessageType) {
    SimpleMessageListenerContainer container =
        new SimpleMessageListenerContainer(connectionFactory);
    container.setQueueNames(queueName);
    container.setConcurrentConsumers(consumers);
    MessageErrorHandler messageErrorHandler =
        new MessageErrorHandler(
            exceptionManagerClient,
            expectedMessageType,
            logStackTraces,
            "Notify Processor",
            queueName);
    container.setErrorHandler(messageErrorHandler);
    container.setChannelTransacted(true);
    return container;
  }
}
