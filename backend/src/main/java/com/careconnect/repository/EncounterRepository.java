package com.careconnect.repository;

import com.careconnect.model.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EncounterRepository extends JpaRepository<Encounter, Long> {
    List<Encounter> findAllByOrderByDateDescTimeDesc();
    List<Encounter> findByPatientIdOrderByDateDescTimeDesc(Long patientId);
}
