package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "toss_payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPayment {

    @Id
    @Column(name = "payment_id", columnDefinition = "BINARY(16)")
    private UUID paymentId;

    // TossPayment : Order = N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @Column(name = "toss_order_id", nullable = false, length = 255)
    private String tossOrderId;

    @Column(name = "toss_payment_key", nullable = false, length = 255)
    private String tossPaymentKey;

    @Column(name = "toss_payment_method", nullable = false)
    private String tossPaymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "toss_payment_status", nullable = false)
    private TossPaymentStatus tossPaymentStatus;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    private String cancelReason;

    // 취소 상태 변경
    public void cancel(String cancelReason) {
        this.tossPaymentStatus = TossPaymentStatus.CANCELED;
        this.cancelReason = cancelReason;
    }

}
