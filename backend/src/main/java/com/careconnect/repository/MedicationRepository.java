package com.careconnect.repository;

import com.careconnect.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findAllByOrderByStatusAscNameAsc();
    List<Medication> findByPatientIdOrderByStatusAscNameAsc(Long patientId);
}
