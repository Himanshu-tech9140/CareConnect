package com.careconnect.repository;

import com.careconnect.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByAppointmentDateAscStartTimeAsc();
    List<Appointment> findByPatientIdOrderByAppointmentDateAscStartTimeAsc(Long patientId);
    boolean existsByProviderIdAndAppointmentDateAndStartTimeLessThanAndEndTimeGreaterThan(Long providerId, LocalDate date, java.time.LocalTime endTime, java.time.LocalTime startTime);
}
