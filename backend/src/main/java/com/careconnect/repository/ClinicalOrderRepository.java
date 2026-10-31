package com.careconnect.repository;

import com.careconnect.model.ClinicalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClinicalOrderRepository extends JpaRepository<ClinicalOrder, Long> {
    List<ClinicalOrder> findAllByOrderByOrderedAtDesc();
}
