package uk.gov.ons.census.notifyprocessor.utilities;


import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

@Component
@ActiveProfiles("test")
@EnableRetry
public class RabbitQueueHelper {

  @Autowired private ConnectionFactory connectionFactory;

  @Autowired private RabbitTemplate testRabbitTemplate;

  @Autowired private AmqpAdmin amqpAdmin;

  public BlockingQueue<String> listen(String queueName) {
    BlockingQueue<String> transfer = new ArrayBlockingQueue(50);

    org.springframework.amqp.core.MessageListener messageListener =
        message -> {
          String msgStr = new String(message.getBody());
          transfer.add(msgStr);
        };
    SimpleMessageListenerContainer container =
        new SimpleMessageListenerContainer(connectionFactory);
    container.setMessageListener(messageListener);
    container.setQueueNames(queueName);
    container.start();

    return transfer;
  }

  public void sendMessage(String exchangeName, String routingKey, Object message) {
    testRabbitTemplate.convertAndSend(exchangeName, routingKey, message);
  }

  @Retryable(
      value = {IOException.class},
      maxAttempts = 10,
      backoff = @Backoff(delay = 5000))
  public void purgeQueue(String queueName) {
    amqpAdmin.purgeQueue(queueName);
  }
}
