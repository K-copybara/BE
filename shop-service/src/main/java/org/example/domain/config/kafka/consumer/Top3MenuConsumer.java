package org.example.domain.config.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.Top3Menu.MenuInfoDto;
import org.example.dto.Top3Menu.Top3MenuResponseEvent;
import org.example.domain.menu.repository.MenuRepository;
import org.example.dto.Top3Menu.Top3MenuRequestEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Top3MenuConsumer {

    private final MenuRepository menuRepository;
    private final KafkaTemplate<String, Top3MenuResponseEvent> kafkaTemplate;

    @KafkaListener(topics = "top3-menu.request", groupId = "shop-service-group")
    public void consume(Top3MenuRequestEvent event) {
        log.info("📥 Kafka 수신 - 상위 메뉴 요청: {}", event);

        List<MenuInfoDto> menuInfos = menuRepository.findAllById(event.getMenuIds())
                .stream()
                .map(menu -> new MenuInfoDto(
                        menu.getId(),
                        menu.getMenuName(),
                        menu.getMenuInfo(),
                        menu.getMenuPrice()
                ))
                .toList();

        Top3MenuResponseEvent response =
                new Top3MenuResponseEvent(event.getStoreId(), menuInfos);

        kafkaTemplate.send("top3-menu.response", response);
        log.info("📤 Kafka 응답 발행 완료: {}", response);
    }
}

