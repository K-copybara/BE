package org.example.domain.entity;

public enum TossPaymentStatus {
    APPROVED,
    READY,      // 결제 생성됨
    IN_PROGRESS,// 결제 진행 중
    DONE,       // 결제 완료
    CANCELED,   // 취소됨
    FAILED      // 실패
}