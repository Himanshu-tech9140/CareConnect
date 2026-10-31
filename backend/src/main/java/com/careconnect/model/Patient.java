package com.careconnect.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true) public String mrn;
    @Column(nullable = false) public String firstName;
    @Column(nullable = false) public String lastName;
    public String preferredName;
    public LocalDate dateOfBirth;
    public String sex;
    public String status;
    public String phone;
    public String email;
    public String city;
    public String state;
    public String bloodType;
    public String primaryProvider;
    public String emergencyContactName;
    public String emergencyContactPhone;
    public LocalDate lastVisit;
    public LocalDate nextVisit;
    public String initials;
    public String risk;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_allergies", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "allergy")
    public List<String> allergies = new ArrayList<>();

    public Patient() { }
}
