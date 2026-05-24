package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.enums.AppointmentStatus;
import model.enums.Specialty;

public class Appointment {

    private final String id;
    private final Patient patient;

    private Doctor doctor;
    private Specialty specialty;

    private LocalDateTime datetime;
    private String reason;

    private final boolean presencial; // true = presencial, false = remota

    private final List<Prescription> prescriptions;

    private AppointmentStatus status;

    // Campos de completación de cita
    private String diagnosis;
    private String observations;
    private String recommendedTreatment;
    private String followUp;

    public Appointment(String id,
            Patient patient,
            Doctor doctor,
            Specialty specialty,
            LocalDateTime datetime,
            String reason,
            boolean presencial) {

        this.id = id;
        this.patient = patient;

        this.doctor = doctor;
        this.specialty = specialty;

        this.datetime = datetime;
        this.reason = reason;

        this.presencial = presencial;

        this.status = AppointmentStatus.REQUESTED;

        this.prescriptions = new ArrayList<>();
    }

    // Getters
    public String getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public String getReason() {
        return reason;
    }

    public boolean isPresencial() {
        return presencial;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public List<Prescription> getPrescriptions() {
        return prescriptions;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getObservations() {
        return observations;
    }

    public String getRecommendedTreatment() {
        return recommendedTreatment;
    }

    public String getFollowUp() {
        return followUp;
    }

    // Setters
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public void setRecommendedTreatment(String recommendedTreatment) {
        this.recommendedTreatment = recommendedTreatment;
    }

    public void setFollowUp(String followUp) {
        this.followUp = followUp;
    }

    // Prescription management
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }

    public void removePrescription(Prescription prescription) {
        prescriptions.remove(prescription);
    }

    @Override
    public String toString() {
        return patient + " | " + datetime;
    }
}
