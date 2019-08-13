package uk.gov.ons.census.notifyprocessor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientApi;

@Configuration
public class NotifyConfiguration {

  @Value("${notify.apiKey}")
  private String apiKey;

  @Value("${notify.baseUrl}")
  private String baseUrl;

  @Bean
  public NotificationClientApi notificationClient() {

    return new NotificationClient(apiKey, baseUrl);

  }


}
