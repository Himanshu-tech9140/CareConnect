package com.careconnect.repository;

import com.careconnect.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrMrnContainingIgnoreCase(String first, String last, String mrn);
    Optional<Patient> findByMrn(String mrn);
}
