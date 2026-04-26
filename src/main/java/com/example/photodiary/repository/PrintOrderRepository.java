package com.example.photodiary.repository;

import com.example.photodiary.model.PrintOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintOrderRepository extends JpaRepository<PrintOrder, Long> {
    List<PrintOrder> findByReceiverName(String receiverName);
}
