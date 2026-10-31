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
import java.time.LocalDateTime;

@Entity
@Table(name = "clinical_orders")
public class ClinicalOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "patient_id") public Patient patient;
    public String category;
    public String item;
    public String priority;
    public String status;
    public String orderedBy;
    public LocalDateTime orderedAt;
    public LocalDate due;
    public String activity;
    public ClinicalOrder() { }
}
