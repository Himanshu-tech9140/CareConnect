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

@Entity
@Table(name = "medications")
public class Medication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "patient_id") public Patient patient;
    public String name;
    public String strength;
    public String route;
    public String frequency;
    public String status;
    public String prescribedBy;
    public LocalDate startDate;
    public LocalDate endDate;
    public String refill;
    public String instructions;

    public Medication() { }
}
