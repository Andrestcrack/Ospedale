package model;

/**
 *
 * @author jjlora
 */
public class Prescription {
    
    private String medicationName;
    private String dose;
    private String administrationRoute;
    private String frequency;
    private String treatmentDuration;
    private String additionalInstructions;

    public Prescription(String medicationName, String dose, String administrationRoute, String frequency, String treatmentDuration, String additionalInstructions) {
        this.medicationName = medicationName;
        this.dose = dose;
        this.administrationRoute = administrationRoute;
        this.frequency = frequency;
        this.treatmentDuration = treatmentDuration;
        this.additionalInstructions = additionalInstructions;
    }
    
    // --- GETTERS (Obligatorios para que el JsonManager los pueda guardar) ---
    public String getMedicationName() { return medicationName; }
    public String getDose() { return dose; }
    public String getAdministrationRoute() { return administrationRoute; }
    public String getFrequency() { return frequency; }
    public String getTreatmentDuration() { return treatmentDuration; }
    public String getAdditionalInstructions() { return additionalInstructions; }

    // --- SETTERS ---
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public void setDose(String dose) { this.dose = dose; }
    public void setAdministrationRoute(String administrationRoute) { this.administrationRoute = administrationRoute; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setTreatmentDuration(String treatmentDuration) { this.treatmentDuration = treatmentDuration; }
    public void setAdditionalInstructions(String additionalInstructions) { this.additionalInstructions = additionalInstructions; }
}