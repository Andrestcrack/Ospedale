package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Patient extends User {

    private String email;
    private LocalDate birthdate;
    private boolean gender;
    private long phone;
    private String address;
    private final List<Appointment> appointments;
    private Hospitalization hospitalization;

    public Patient(long id, String username, String firstname, String lastname,
                   String password, String email, LocalDate birthdate,
                   boolean gender, long phone, String address) {
        super(id, username, firstname, lastname, password);
        this.email = email;
        this.birthdate = birthdate;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.appointments = new ArrayList<>();
    }

    // Getters
    public String getEmail()               { return email; }
    public LocalDate getBirthdate()        { return birthdate; }
    public boolean isGender()              { return gender; }
    public long getPhone()                 { return phone; }
    public String getAddress()             { return address; }
    public List<Appointment> getAppointments() { return appointments; }
    public Hospitalization getHospitalization() { return hospitalization; }

    // Setters
    public void setEmail(String email)             { this.email = email; }
    public void setBirthdate(LocalDate birthdate)  { this.birthdate = birthdate; }
    public void setGender(boolean gender)          { this.gender = gender; }
    public void setPhone(long phone)               { this.phone = phone; }
    public void setAddress(String address)         { this.address = address; }
    public void setHospitalization(Hospitalization h) { this.hospitalization = h; }

    public void addAppointment(Appointment a) {
        this.appointments.add(a);
    }
    
    public void removeAppointment(Appointment appointment) {
    appointments.remove(appointment);
}
    
    @Override
public String toString() {
    return getFirstname() + " " + getLastname();
}

}
