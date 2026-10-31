package com.careconnect.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="provider_profiles")
public class ProviderProfile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @OneToOne(fetch=FetchType.EAGER, optional=false) @JoinColumn(name="user_id", unique=true) public AppUser user;
    public String specialization;
    public String qualification;
    public String licenseNumber;
    public String department;
    public boolean active=true;
    public ProviderProfile() { }
}
