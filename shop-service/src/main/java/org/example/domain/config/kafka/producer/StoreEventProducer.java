package org.example.domain.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.entity.Store;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStoreUpdatedEvent(Long storeId, String eventType, Store store) {
        Map<String, List<String>> parsedHours = parseBusinessHours(store.getBusinessHours());

        Map<String, Object> storeInfo = Map.of(
                "name", store.getShopName(),
                "description", store.getShopInfo(),
                "hours", parsedHours.get("hours"),
                "break_time", parsedHours.get("break_time")
        );

        Map<String, Object> message = Map.of(
                "storeId", storeId,
                "eventType", eventType,   // CREATED, UPDATED, DELETED
                "timestamp", LocalDateTime.now().toString(),
                "store", storeInfo
        );

        kafkaTemplate.send("store-updated", message);
        log.info("📤 Kafka 전송 완료 → Topic: store-updated, Payload: {}", message);
    }

    /**
     * businessHours 문자열을 일~토 순서 배열로 변환
     * 예: SUNDAY:11:30,23:00,15:00,16:30;MONDAY:11:00,23:00,,;
     */
    private Map<String, List<String>> parseBusinessHours(String businessHours) {
        List<String> daysOfWeek = List.of("SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY");
        Map<String, String[]> hoursMap = new HashMap<>();

        if (businessHours != null && !businessHours.isBlank()) {
            for (String entry : businessHours.split(";")) {
                if (entry.isBlank()) continue;
                String[] parts = entry.split(":", 2);
                if (parts.length < 2) continue;

                String day = parts[0].trim().toUpperCase();
                String[] times = parts[1].split(",");

                String open = times.length > 0 && !times[0].isBlank() ? times[0] : null;
                String close = times.length > 1 && !times[1].isBlank() ? times[1] : null;
                String breakOpen = times.length > 2 && !times[2].isBlank() ? times[2] : null;
                String breakClose = times.length > 3 && !times[3].isBlank() ? times[3] : null;

                hoursMap.put(day, new String[]{open, close, breakOpen, breakClose});
            }
        }

        List<String> hours = new ArrayList<>();
        List<String> breaks = new ArrayList<>();
        for (String day : daysOfWeek) {
            String[] t = hoursMap.getOrDefault(day, new String[]{null, null, null, null});
            hours.add(t[0] != null && t[1] != null ? t[0] + "-" + t[1] : null);
            breaks.add(t[2] != null && t[3] != null ? t[2] + "-" + t[3] : null);
        }

        return Map.of("hours", hours, "break_time", breaks);
    }
}

