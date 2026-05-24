package model;

import java.util.ArrayList;

/**
 *
 * @author edangulo
 */
public class Doctor extends User {
    
    private Specialty specialty;
    private String licenseNumber; // Corregido: "s" en lugar de "c"
    private String assignedOffice;
    private ArrayList<Appointment> appointments;
    private ArrayList<Hospitalization> hospitalizations;

    public Doctor(long id, String username, String firstname, String lastname, String password, Specialty specialty, String licenseNumber, String assignedOffice) {
        super(id, username, firstname, lastname, password);
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
        this.assignedOffice = assignedOffice;
        
        // ¡Corrección vital! Inicializar ambas listas para evitar NullPointerException
        this.hospitalizations = new ArrayList<>();
        this.appointments = new ArrayList<>(); 
    }

    // --- GETTERS (Vitales para que el JsonManager pueda guardar los datos) ---

    public Specialty getSpecialty() {
        return specialty;
    }

    public String getLicenseNumber() { // Corregido: "s" en lugar de "c"
        return licenseNumber;
    }

    public String getAssignedOffice() {
        return assignedOffice;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }
    
    public ArrayList<Hospitalization> getHospitalizations() {
        return hospitalizations;
    }

    // --- SETTERS Y MÉTODOS DE NEGOCIO ---
    
    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public void setLicenseNumber(String licenseNumber) { // Corregido: "s" en lugar de "c"
        this.licenseNumber = licenseNumber;
    }

    public void setAssignedOffice(String assignedOffice) {
        this.assignedOffice = assignedOffice;
    }

    public boolean addHospitalization(Hospitalization hosp){
        return hospitalizations.add(hosp);
    }
    
    // (Opcional) Método para agregar citas más adelante
    public boolean addAppointment(Appointment appt) {
        return appointments.add(appt);
    }
}