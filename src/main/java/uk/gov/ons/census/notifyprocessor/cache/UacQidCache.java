package uk.gov.ons.census.notifyprocessor.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.notifyprocessor.client.UacQidServiceClient;
import uk.gov.ons.census.notifyprocessor.model.UacQid;

@Component
public class UacQidCache {
  private final UacQidServiceClient uacQidServiceClient;

  @Value("${uacservice.uacqid-cache-min}")
  private int cacheMin;

  @Value("${uacservice.uacqid-fetch-count}")
  private int cacheFetch;

  @Value("${uacservice.uacqid-get-timeout}")
  private long uacQidGetTimout;

  private static final Executor executor = Executors.newFixedThreadPool(8);

  private Map<Integer, BlockingQueue<UacQid>> uacQidLinkQueueMap = new ConcurrentHashMap<>();
  private Set<Integer> isToppingUpQueue = ConcurrentHashMap.newKeySet();

  public UacQidCache(UacQidServiceClient uacQidServiceClient) {
    this.uacQidServiceClient = uacQidServiceClient;
  }

  public UacQid getUacQidPair(int questionnaireType) {
    uacQidLinkQueueMap.computeIfAbsent(questionnaireType, key -> new LinkedBlockingDeque<>());

    try {
      topUpQueue(questionnaireType);
      UacQid uacQid =
          uacQidLinkQueueMap.get(questionnaireType).poll(uacQidGetTimout, TimeUnit.SECONDS);

      if (uacQid == null) {
        // The cache topper upper is executed in a separate thread, which can fail if uacqid api
        // down
        // So check we get a non null result otherwise throw a RunTimeException to re-enqueue msg
        throw new RuntimeException(
            "Timeout getting UAC QID for questionnaireType :" + questionnaireType);
      }

      return uacQid;
    } catch (InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private void topUpQueue(int questionnaireType) {
    synchronized (isToppingUpQueue) {
      if (!isToppingUpQueue.contains(questionnaireType)
          && uacQidLinkQueueMap.get(questionnaireType).size() < cacheMin) {
        isToppingUpQueue.add(questionnaireType);
      } else {
        return;
      }
    }

    executor.execute(
        () -> {
          try {
            uacQidLinkQueueMap
                .get(questionnaireType)
                .addAll(uacQidServiceClient.getUacQids(questionnaireType, cacheFetch));
          } finally {
            isToppingUpQueue.remove(questionnaireType);
          }
        });
  }
}
