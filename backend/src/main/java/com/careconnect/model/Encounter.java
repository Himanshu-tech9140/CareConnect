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
@Table(name = "encounters")
public class Encounter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "patient_id") public Patient patient;
    public String provider;
    public LocalDate date;
    public LocalTime time;
    public String type;
    public String reason;
    public String status;
    public String location;
    public String notePreview;
    public String chiefComplaint;
    public String history;
    public String examination;
    public String assessment;
    public String plan;
    public String followUp;
    public String noteStatus;

    public Encounter() { }
}
