package com.careconnect.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

record NoteUpdateRequest(@NotBlank String history, @NotBlank String examination, @NotBlank String assessment, @NotBlank String plan, @NotBlank String followUp, @NotBlank String noteStatus) { }
record OrderCreateRequest(@NotNull @Positive Long patientId, @NotBlank String category, @NotBlank String item, @NotBlank String priority, @NotBlank String due) { }
record StatusUpdateRequest(@NotBlank String status) { }
record MedicationCreateRequest(@NotNull @Positive Long patientId, @NotBlank String name, @NotBlank String strength, @NotBlank String route, @NotBlank String frequency, @NotBlank String instructions, boolean allergyAcknowledged) { }
record LoginRequest(@NotBlank String username, @NotBlank String password) { }
record PatientCreateRequest(@NotBlank String firstName, @NotBlank String lastName, String dateOfBirth, String sex, @NotBlank String phone, @NotBlank String email, String city, String state, String emergencyContactName, String emergencyContactPhone) { }
record AppointmentCreateRequest(@NotNull @Positive Long patientId, @NotNull @Positive Long providerId, @NotBlank String date, @NotBlank String startTime, @NotBlank String endTime, @NotBlank String type, @NotBlank String location) { }
record ProviderProfileRequest(@NotNull @Positive Long userId, @NotBlank String specialization, @NotBlank String qualification, @NotBlank String licenseNumber, @NotBlank String department, boolean active) { }
record SignupRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9._-]{3,160}", message="must be 3-160 letters, numbers, dots, underscores, or hyphens") String username, @NotBlank @Size(min=8, max=72) String password, @NotBlank String firstName, @NotBlank String lastName, @NotBlank String email, @NotBlank String phone, String dateOfBirth) { }
record StaffCreateRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9._-]{3,160}", message="must be 3-160 letters, numbers, dots, underscores, or hyphens") String username, @NotBlank @Size(min=8, max=72) String password, @NotBlank String displayName, @NotBlank String role, String specialization, String qualification, String licenseNumber, @NotBlank String department) { }
