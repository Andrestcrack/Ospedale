package model;

import java.util.ArrayList;
import java.util.List;
import model.enums.Specialty;

public class Doctor extends User {

    private Specialty specialty;
    private String licenseNumber;
    private String assignedOffice;
    private final List<Appointment> appointments;
    private final List<Hospitalization> hospitalizations;

    public Doctor(long id, String username, String firstname, String lastname,
            String password, Specialty specialty,
            String licenceNumber, String assignedOffice) {
        super(id, username, firstname, lastname, password);
        this.specialty = specialty;
        this.licenseNumber = licenceNumber;
        this.assignedOffice = assignedOffice;
        this.appointments = new ArrayList<>();
        this.hospitalizations = new ArrayList<>();
    }

    // Getters
    public Specialty getSpecialty() {
        return specialty;
    }

    public String getLicenceNumber() {
        return licenseNumber;
    }

    public String getAssignedOffice() {
        return assignedOffice;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public List<Hospitalization> getHospitalizations() {
        return hospitalizations;
    }

    // Setters
    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenseNumber = licenceNumber;
    }

    public void setAssignedOffice(String assignedOffice) {
        this.assignedOffice = assignedOffice;
    }

    /**
     * Añade una cita a la lista del doctor.
     */
    public void addAppointment(Appointment a) {
        this.appointments.add(a);
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
    }

    /**
     * Añade una hospitalización a la lista del doctor.
     */
    public void addHospitalization(Hospitalization h) {
        this.hospitalizations.add(h);
    }

    public void removeHospitalization(Hospitalization hospitalization) {
        hospitalizations.remove(hospitalization);
    }

    @Override
    public String toString() {
        return getFirstname() + " " + getLastname();
    }
}
