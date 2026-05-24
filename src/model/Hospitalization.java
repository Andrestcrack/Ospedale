package model;

import java.time.LocalDate;

public class Hospitalization {
    private String id;
    private Patient patient;
    private Doctor doctor;
    private LocalDate admissionDate;
    private String reason;
    private String roomType;
    private String observations;
    private String duration; 
    private HospitalizationStatus status;

    public Hospitalization(String id, Patient patient, Doctor doctor, LocalDate admissionDate, String reason, String roomType, String observations) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.admissionDate = admissionDate;
        this.reason = reason;
        this.roomType = roomType;
        this.observations = observations;
        this.status = HospitalizationStatus.REQUESTED; // Por defecto según el PDF
    }

    public String getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public String getReason() { return reason; }
    public String getRoomType() { return roomType; }
    public String getObservations() { return observations; }
    public String getDuration() { return duration; }
    public HospitalizationStatus getStatus() { return status; }

    public void setStatus(HospitalizationStatus status) { this.status = status; }
    public void setDuration(String duration) { this.duration = duration; }
}