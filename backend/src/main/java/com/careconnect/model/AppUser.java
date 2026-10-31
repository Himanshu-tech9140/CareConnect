package com.careconnect.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String username;
    public String displayName;
    public String role;
    public String initials;
    public String subtitle;
    public Long patientId;
    public String passwordHash;
    public boolean active = true;

    public AppUser() { }
}
