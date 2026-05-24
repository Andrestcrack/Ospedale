package model;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Appointment {
    
    private final String id;
    private Patient patient;
    private Doctor doctor;
    private Specialty specialty;
    private LocalDateTime datetime;
    private String reason;
    private boolean type;
    private ArrayList<Prescription> prescriptions;
    private AppointmentStatus status;
    private String diagnosis;
    private String observations;
    private String recommendedTreatment;
    private String followUp;

    public Appointment(String id, Patient patient, Doctor doctor, Specialty specialty, LocalDateTime datetime, String reason, boolean type) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.specialty = specialty;
        this.datetime = datetime;
        this.reason = reason;
        this.type = type;
        this.status = AppointmentStatus.REQUESTED;
        this.prescriptions = new ArrayList<>();
    }
    
    // --- GETTERS COMPLETOS (Vitales para JSON y Tablas) ---
    public String getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public Specialty getSpecialty() { return specialty; }
    public LocalDateTime getDatetime() { return datetime; }
    public boolean isType() { return type; }
    public String getReason() { return reason; }
    public AppointmentStatus getStatus() { return status; }
    public String getDiagnosis() { return diagnosis; }
    public String getObservations() { return observations; }
    public String getRecommendedTreatment() { return recommendedTreatment; }
    public String getFollowUp() { return followUp; }
    public ArrayList<Prescription> getPrescriptions() { return prescriptions; }

    // --- SETTERS ---
    public void setReason(String reason) { this.reason = reason; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setObservations(String observations) { this.observations = observations; }
    public void setRecommendedTreatment(String recommendedTreatment) { this.recommendedTreatment = recommendedTreatment; }
    public void setFollowUp(String followUp) { this.followUp = followUp; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public void setDatetime(LocalDateTime datetime) { this.datetime = datetime; }
    
    // --- MÉTODOS DE PRESCRIPCIONES ---
    public void setPrescriptions(ArrayList<Prescription> prescriptions) { 
        this.prescriptions = prescriptions; 
    }

    public boolean addPrescription(Prescription prescrip) {
        if (this.prescriptions == null) {
            this.prescriptions = new ArrayList<>();
        }
        return this.prescriptions.add(prescrip);
    }
}