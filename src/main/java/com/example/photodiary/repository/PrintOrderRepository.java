package com.example.photodiary.repository;

import com.example.photodiary.model.PrintOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintOrderRepository extends JpaRepository<PrintOrder, Long> {
}
