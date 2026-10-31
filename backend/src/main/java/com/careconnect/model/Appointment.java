package com.careconnect.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="appointments")
public class Appointment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="patient_id") public Patient patient;
    @ManyToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="provider_id") public AppUser provider;
    public LocalDate appointmentDate;
    public LocalTime startTime;
    public LocalTime endTime;
    public String type;
    public String location;
    public String status;
    public Appointment() { }
}
