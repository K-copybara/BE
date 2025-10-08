package org.example.domain.stats.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.Top3Menu.Top3MenuResponseEvent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CompletableFutureService {

    private final Map<Long, CompletableFuture<Top3MenuResponseEvent>> responseMap = new ConcurrentHashMap<>();

    public CompletableFuture<Top3MenuResponseEvent> createFuture(Long storeId) {
        CompletableFuture<Top3MenuResponseEvent> future = new CompletableFuture<>();
        responseMap.put(storeId, future);
        return future;
    }

    public void completeResponse(Long storeId, Top3MenuResponseEvent event) {
        CompletableFuture<Top3MenuResponseEvent> future = responseMap.remove(storeId);
        if (future != null) {
            future.complete(event);
            log.info("✅ Kafka 응답 완료 - storeId={}", storeId);
        }
    }

    public Top3MenuResponseEvent waitForResponse(Long storeId, long timeout, TimeUnit unit) throws Exception {
        CompletableFuture<Top3MenuResponseEvent> future = createFuture(storeId);
        return future.get(timeout, unit);
    }
}
