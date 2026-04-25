package com.example.photodiary.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PrintOrder {

    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    public boolean isProcessing() {
        return "PROCESSING".equals(this.status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }

    public String getDisplayPostIds() {
        if (this.orderDetails == null || this.orderDetails.isEmpty()) {
            return "없음";
        }
        // JSON 특수문자([], ") 제거하여 숫자만 남김
        return this.orderDetails.replace("[", "").replace("]", "").replace("\"", "");
    }

    public String getStatusColor() {
        if ("PENDING".equals(this.status)) return "bg-secondary";
        if ("PROCESSING".equals(this.status)) return "bg-warning text-dark";
        if ("COMPLETED".equals(this.status)) return "bg-success";
        return "bg-dark";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 상태 관리 (pending -> processing -> completed) [cite: 29]
    private String status;

    private LocalDateTime orderDate;

    private String shippingAddress;

    // 주문된 게시글 Id 모음
    @Column(columnDefinition = "TEXT")
    private String orderDetails;

    @PrePersist
    public void prePersist() {
        this.status = "PENDING"; // 초기 상태 [cite: 29]
        this.orderDate = LocalDateTime.now();
    }

    public String getFormattedDate() {
        if (this.orderDate == null) return "";
        return this.orderDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
