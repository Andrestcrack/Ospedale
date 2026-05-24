package model;

public class Prescription {

    private final Appointment appointment;

    private final String medicationName;
    private final double dose;

    private final String administrationRoute;
    private final int treatmentDuration;

    private final String additionalInstructions;
    private final int frequency;

    public Prescription(Appointment appointment,
            String medicationName,
            double dose,
            String administrationRoute,
            int treatmentDuration,
            String additionalInstructions,
            int frequency) {

        this.appointment = appointment;

        this.medicationName = medicationName;
        this.dose = dose;

        this.administrationRoute = administrationRoute;
        this.treatmentDuration = treatmentDuration;

        this.additionalInstructions = additionalInstructions;
        this.frequency = frequency;
    }

    // Getters
    public Appointment getAppointment() {
        return appointment;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public double getDose() {
        return dose;
    }

    public String getAdministrationRoute() {
        return administrationRoute;
    }

    public int getTreatmentDuration() {
        return treatmentDuration;
    }

    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return medicationName + " (" + dose + ")";
    }
}
