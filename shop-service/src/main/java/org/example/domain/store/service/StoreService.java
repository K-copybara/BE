package org.example.domain.store.service;


import lombok.RequiredArgsConstructor;
import org.example.domain.config.kafka.producer.StoreEventProducer;
import org.example.domain.entity.Store;
import org.example.domain.store.dto.request.StoreBusinessHoursUpdateRequestDto;
import org.example.domain.store.dto.request.StoreNoticeUpdateRequestDto;
import org.example.domain.store.dto.response.BusinessHoursDetailDto;
import org.example.domain.store.dto.response.StoreResponseDto;
import org.example.domain.store.repository.StoreRepository;
import org.example.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreEventProducer storeEventProducer;

    // 상점 정보 조회
    public StoreResponseDto getStoreInfoByEmail(String email) {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        List<BusinessHoursDetailDto> hours = parseBusinessHours(store.getBusinessHours());

        return StoreResponseDto.builder()
                .storeId(store.getId())
                .shopName(store.getShopName())
                .notice(store.getNotice())
                .businessHoursDetail(hours)
                .build();
    }

    private List<BusinessHoursDetailDto> parseBusinessHours(String businessHoursText) {
        if (businessHoursText == null || businessHoursText.isBlank()) {
            // DB에 아무 데이터가 없어도 전체 요일을 null로 채워 반환
            return initializeEmptyWeek();
        }

        Map<String, BusinessHoursDetailDto> parsed = new HashMap<>();

        // 예: "SUNDAY:11:00,23:00,15:00,16:30;MONDAY:11:00,23:00,null,null"
        Arrays.stream(businessHoursText.split(";"))
                .filter(entry -> entry != null && entry.contains(":"))
                .forEach(entry -> {
                    String[] parts = entry.split(":", 2);
                    if (parts.length < 2) return; // 안전 처리

                    String day = parts[0].trim().toUpperCase();
                    String[] times = parts[1].split(",");

                    parsed.put(day, BusinessHoursDetailDto.builder()
                            .dayOfWeek(day)
                            .openTime(getSafe(times, 0))
                            .closeTime(getSafe(times, 1))
                            .breakOpenTime(getSafe(times, 2))
                            .breakCloseTime(getSafe(times, 3))
                            .build());
                });

        return fillMissingDays(parsed);
    }

    // 누락 요일 보정
    private List<BusinessHoursDetailDto> fillMissingDays(Map<String, BusinessHoursDetailDto> parsed) {
        List<String> allDays = List.of(
                "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY",
                "THURSDAY", "FRIDAY", "SATURDAY"
        );

        return allDays.stream()
                .map(day -> parsed.getOrDefault(day,
                        BusinessHoursDetailDto.builder()
                                .dayOfWeek(day)
                                .openTime(null)
                                .closeTime(null)
                                .breakOpenTime(null)
                                .breakCloseTime(null)
                                .build()
                ))
                .collect(Collectors.toList());
    }

    // 문자열 안전 접근
    private String getSafe(String[] arr, int idx) {
        if (arr == null || arr.length <= idx) return null;
        String val = arr[idx].trim();
        return val.isBlank() || val.equalsIgnoreCase("null") ? null : val;
    }

    // 초기화: 모든 요일 null로 생성
    private List<BusinessHoursDetailDto> initializeEmptyWeek() {
        return List.of(
                        "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY",
                        "THURSDAY", "FRIDAY", "SATURDAY"
                ).stream()
                .map(day -> BusinessHoursDetailDto.builder()
                        .dayOfWeek(day)
                        .openTime(null)
                        .closeTime(null)
                        .breakOpenTime(null)
                        .breakCloseTime(null)
                        .build())
                .collect(Collectors.toList());
    }


    // 공지사항 수정
    @Transactional
    public Response<Void> updateStoreNotice(String email, StoreNoticeUpdateRequestDto requestDto) {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점 정보를 찾을 수 없습니다."));

        store.changeNotice(requestDto.getNotice());

        // AI 서버 자동 업데이트 이벤트 발행
        storeEventProducer.sendStoreUpdatedEvent(store.getId(), "UPDATED", store);

        return Response.success("공지 수정 성공", null);
    }

    // 영업시간 수정
    @Transactional
    public Response<Void> updateBusinessHours(String email, StoreBusinessHoursUpdateRequestDto requestDto) {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("상점을 찾을 수 없습니다."));

        store.changeBusinessHours(requestDto.getBusinessHours()); // 도메인 행위 호출

        // AI 서버 자동 업데이트 이벤트 발행
        storeEventProducer.sendStoreUpdatedEvent(store.getId(), "UPDATED", store);

        return Response.success("영업 시간 수정 성공", null);
    }
}
