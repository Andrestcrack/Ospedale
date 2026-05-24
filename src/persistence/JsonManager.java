package persistence;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import model.User;
import model.Patient;
import model.Doctor;
import model.Administrator;
import model.Specialty;
import model.Appointment;
import model.AppointmentStatus;
import model.Hospitalization;
import model.HospitalizationStatus;

public class JsonManager {
    
    private static final String FILE_PATH = "json/users.json";

    public static void guardarUsuarios(ArrayList<User> usuarios) {
        JSONObject rootJson = new JSONObject();
        JSONArray listaJson = new JSONArray();

        for (User usuario : usuarios) {
            JSONObject obj = new JSONObject();
            
            obj.put("id", usuario.getId());
            obj.put("username", usuario.getUsername());
            obj.put("password", usuario.getPassword());
            obj.put("firstname", usuario.getFirstname());
            obj.put("lastname", usuario.getLastname());
            
            if (usuario instanceof Patient) {
                obj.put("type", "patient");
                Patient p = (Patient) usuario;
                obj.put("email", p.getEmail());
                obj.put("birthdate", p.getBirthdate().toString()); 
                obj.put("gender", p.getGender());
                obj.put("phone", p.getPhone());
                obj.put("address", p.getAddress());
                
                JSONArray citasJson = new JSONArray();
                if (p.getAppointments() != null) {
                    for (Appointment cita : p.getAppointments()) {
                        JSONObject citaObj = new JSONObject();
                        citaObj.put("id", cita.getId());
                        citaObj.put("datetime", cita.getDatetime().toString());
                        citaObj.put("reason", cita.getReason());
                        citaObj.put("type", cita.isType());
                        if (cita.getStatus() != null) citaObj.put("status", cita.getStatus().name());
                        
                        if (cita.getSpecialty() != null) citaObj.put("specialty", cita.getSpecialty().name());
                        if (cita.getDoctor() != null) citaObj.put("doctorId", cita.getDoctor().getId());
                        citasJson.put(citaObj);
                    }
                }
                obj.put("appointments", citasJson);

                // NUEVO: Guardar Hospitalizaciones
                JSONArray hospitalizacionesJson = new JSONArray();
                if (p.getHospitalizations() != null) {
                    for (Hospitalization h : p.getHospitalizations()) {
                        JSONObject hObj = new JSONObject();
                        hObj.put("id", h.getId());
                        hObj.put("admissionDate", h.getAdmissionDate() != null ? h.getAdmissionDate().toString() : LocalDate.now().toString());
                        hObj.put("reason", h.getReason());
                        hObj.put("roomType", h.getRoomType());
                        hObj.put("observations", h.getObservations());
                        if (h.getStatus() != null) hObj.put("status", h.getStatus().name());
                        if (h.getDoctor() != null) hObj.put("doctorId", h.getDoctor().getId());
                        if (h.getDuration() != null) hObj.put("duration", h.getDuration());
                        hospitalizacionesJson.put(hObj);
                    }
                }
                obj.put("hospitalizations", hospitalizacionesJson);
                
            } else if (usuario instanceof Doctor) {
                obj.put("type", "doctor");
                Doctor d = (Doctor) usuario;
                obj.put("specialty", d.getSpecialty().name());
                obj.put("licenceNumber", d.getLicenseNumber());
                obj.put("assignedOffice", d.getAssignedOffice());
                
            } else if (usuario instanceof Administrator) {
                obj.put("type", "admin");
            }
            
            listaJson.put(obj);
        }

        rootJson.put("users", listaJson);

        try (FileWriter file = new FileWriter(FILE_PATH)) {
            file.write(rootJson.toString(2));
        } catch (Exception e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    public static ArrayList<User> cargarUsuarios() {
        ArrayList<User> listaRecuperada = new ArrayList<>();
        
        try {
            String contenido = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            JSONObject rootJson = new JSONObject(contenido);
            JSONArray listaJson = rootJson.getJSONArray("users");
            
            // PRIMERA PASADA: Cargar personas
            for (int i = 0; i < listaJson.length(); i++) {
                JSONObject obj = listaJson.getJSONObject(i);
                long id = obj.getLong("id");
                String username = obj.getString("username");
                String password = obj.getString("password");
                String type = obj.getString("type");
                String firstname = obj.optString("firstname", "");
                String lastname = obj.optString("lastname", "");
                
                if (type.equals("patient")) {
                    String email = obj.optString("email", "");
                    LocalDate birthdate = LocalDate.parse(obj.optString("birthdate", "2000-01-01"));
                    boolean gender = obj.optBoolean("gender", true);
                    long phone = obj.optLong("phone", 0);
                    String address = obj.optString("address", "");
                    
                    listaRecuperada.add(new Patient(id, username, firstname, lastname, password, email, birthdate, gender, phone, address));
                    
                } else if (type.equals("doctor")) {
                    Specialty specialty = Specialty.valueOf(obj.getString("specialty"));
                    String licenceNumber = obj.getString("licenceNumber"); 
                    String assignedOffice = obj.getString("assignedOffice");
                    listaRecuperada.add(new Doctor(id, username, firstname, lastname, password, specialty, licenceNumber, assignedOffice));
                    
                } else if (type.equals("admin")) {
                    listaRecuperada.add(new Administrator(id, username, firstname, lastname, password));
                }
            }
            
            // SEGUNDA PASADA: Enlazar Citas y Hospitalizaciones
            for (int i = 0; i < listaJson.length(); i++) {
                JSONObject obj = listaJson.getJSONObject(i);
                if (obj.getString("type").equals("patient")) {
                    
                    Patient pacienteActual = null;
                    for (User u : listaRecuperada) {
                        if (u.getId() == obj.getLong("id")) { pacienteActual = (Patient) u; break; }
                    }
                    
                    if (obj.has("appointments")) {
                        JSONArray citasJson = obj.getJSONArray("appointments");
                        for (int j = 0; j < citasJson.length(); j++) {
                            JSONObject citaObj = citasJson.getJSONObject(j);
                            String idCita = citaObj.getString("id");
                            LocalDateTime datetime = LocalDateTime.parse(citaObj.getString("datetime"));
                            String reason = citaObj.getString("reason");
                            boolean citaType = citaObj.getBoolean("type");
                            
                            Specialty spec = citaObj.has("specialty") ? Specialty.valueOf(citaObj.getString("specialty")) : null;
                            Doctor doctorAsignado = null;
                            if (citaObj.has("doctorId")) {
                                long doctorId = citaObj.getLong("doctorId");
                                for (User u : listaRecuperada) if (u.getId() == doctorId && u instanceof Doctor) { doctorAsignado = (Doctor) u; break; }
                            }
                            
                            Appointment cita = new Appointment(idCita, pacienteActual, doctorAsignado, spec, datetime, reason, citaType);
                            if (citaObj.has("status")) cita.setStatus(AppointmentStatus.valueOf(citaObj.getString("status")));
                            pacienteActual.addAppointment(cita);
                        }
                    }

                    // NUEVO: Cargar Hospitalizaciones
                    if (obj.has("hospitalizations")) {
                        JSONArray hospJson = obj.getJSONArray("hospitalizations");
                        for (int k = 0; k < hospJson.length(); k++) {
                            JSONObject hObj = hospJson.getJSONObject(k);
                            String hId = hObj.getString("id");
                            LocalDate admissionDate = LocalDate.parse(hObj.getString("admissionDate"));
                            String reason = hObj.getString("reason");
                            String roomType = hObj.getString("roomType");
                            String obs = hObj.getString("observations");
                            
                            Doctor doctorHosp = null;
                            if (hObj.has("doctorId")) {
                                long docId = hObj.getLong("doctorId");
                                for (User u : listaRecuperada) if (u.getId() == docId && u instanceof Doctor) { doctorHosp = (Doctor) u; break; }
                            }
                            
                            Hospitalization h = new Hospitalization(hId, pacienteActual, doctorHosp, admissionDate, reason, roomType, obs);
                            if (hObj.has("status")) h.setStatus(HospitalizationStatus.valueOf(hObj.getString("status")));
                            if (hObj.has("duration")) h.setDuration(hObj.getString("duration"));
                            
                            if (pacienteActual.getHospitalizations() == null) pacienteActual.setHospitalizations(new ArrayList<>());
                            pacienteActual.getHospitalizations().add(h);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error al cargar JSON: " + e.getMessage());
        }
        
        return listaRecuperada;
    }
}