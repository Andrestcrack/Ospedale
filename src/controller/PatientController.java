package controller;

import view.PatientView;
import view.AdminView;
import view.LoginView;
import model.*;
import persistence.JsonManager;
import response.Response;
import observer.Observer;
import observer.ModelEvent;
import observer.ModelEventBus;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;

public class PatientController implements ActionListener, Observer {

    private PatientView view;
    private Patient pacienteLogueado;
    private boolean isAdmin;

    private final AppointmentController appointmentController = new AppointmentController();

    public PatientController(PatientView view, Patient pacienteLogueado) {
        this(view, pacienteLogueado, false);
    }

    public PatientController(PatientView view, Patient pacienteLogueado, boolean isAdmin) {
        this.view = view;
        this.pacienteLogueado = pacienteLogueado;
        this.isAdmin = isAdmin;
        
        this.view.getBtnBack().setVisible(isAdmin);
        initListeners();
        cargarDatosPacienteEnVista();
        cargarDoctoresEnCombo(); 
        cargarHistorialCitas();
        cargarTiposDeHabitacion();

        // Patrón Observador: registrarse para recibir notificaciones automáticas
        // Cuando AppointmentController o HospitalizationController publiquen un evento,
        // este controlador actualizará la tabla automáticamente.
        ModelEventBus.getInstance().subscribe(this);

        this.view.setVisible(true);
    }

    @Override
    public void onModelChanged(ModelEvent event) {
        // Solo nos interesan los cambios de citas (las hospitalizaciones
        // no tienen tabla propia en la vista del paciente)
        if (event == ModelEvent.APPOINTMENT_CHANGED) {
            cargarHistorialCitas();
        }
    }

    private void initListeners() {
        this.view.getBtnLogout().addActionListener(this);
        this.view.getBtnCreateAppointment().addActionListener(this);
        this.view.getBtnSave().addActionListener(this);
        this.view.getBtnBack().addActionListener(this);
        this.view.getBtnRefresh().addActionListener(this);
        this.view.getBtnCancelAppointment().addActionListener(this);
        this.view.getBtnCreateHospitalization().addActionListener(this);
        this.view.getRdoDoctor().addActionListener(this);
        this.view.getRdoSpecialty().addActionListener(this);
    }

    private void cargarDatosPacienteEnVista() {
        if (pacienteLogueado != null) {
            view.getTxtFirstname().setText(pacienteLogueado.getFirstname());
            view.getTxtLastname().setText(pacienteLogueado.getLastname());
            view.getTxtUsername().setText(pacienteLogueado.getUsername());
            view.getTxtEmail().setText(pacienteLogueado.getEmail());
            view.getTxtPhone().setText(String.valueOf(pacienteLogueado.getPhone()));
            view.getTxtAddress().setText(pacienteLogueado.getAddress());
            view.getTxtBirthdate().setText(pacienteLogueado.getBirthdate().toString());
            view.getCmbGender().setSelectedItem(pacienteLogueado.getGender() ? "Female" : "Male");
        }
    }

    // Ordenamiento Descendente y Refresco en tiempo real
    private void cargarHistorialCitas() {
        // 1. Refrescar al paciente desde el JSON para asegurar que la vista esté sincronizada
        for (User u : JsonManager.cargarUsuarios()) {
            if (u.getId() == pacienteLogueado.getId()) {
                this.pacienteLogueado = (Patient) u;
                break;
            }
        }

        // 2. Limpiar y llenar la tabla
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) view.getTblAppointment().getModel();
        modelo.setRowCount(0);
        view.getCmbCancelAppointmentId().removeAllItems();
        view.getCmbCancelAppointmentId().addItem("Select one");

        if (pacienteLogueado != null && pacienteLogueado.getAppointments() != null) {
            ArrayList<Appointment> citasOrdenadas = new ArrayList<>(pacienteLogueado.getAppointments());
            // Ordenar de la más reciente a la más antigua
            citasOrdenadas.sort((a, b) -> b.getDatetime().compareTo(a.getDatetime()));
            
            for (Appointment cita : citasOrdenadas) {
                modelo.addRow(new Object[]{
                    cita.getId(),
                    cita.getDatetime().toString().replace("T", " "),
                    (cita.getDoctor() != null) ? cita.getDoctor().getFirstname() + " " + cita.getDoctor().getLastname() : "Unassigned",
                    (cita.getSpecialty() != null) ? cita.getSpecialty().name() : "Unassigned",
                    cita.isType() ? "Remote" : "In-person",
                    cita.getStatus().name()
                });
                
                if (cita.getStatus() != AppointmentStatus.COMPLETED && cita.getStatus() != AppointmentStatus.CANCELED) {
                    view.getCmbCancelAppointmentId().addItem(cita.getId());
                }
            }
        }
    }

    private void cargarDoctoresEnCombo() {
        view.getCmbSelection().removeAllItems();
        view.getCmbSelection().addItem("Select one");
        
        view.getCmbAttendingDoctor().removeAllItems();
        view.getCmbAttendingDoctor().addItem("Select one");
        
        for (User u : JsonManager.cargarUsuarios()) {
            if (u instanceof Doctor) {
                Doctor d = (Doctor) u;
                String docInfo = d.getFirstname() + " " + d.getLastname() + " - " + d.getSpecialty().name();
                view.getCmbSelection().addItem(docInfo);
                view.getCmbAttendingDoctor().addItem(docInfo);
            }
        }
    }

    private void cargarEspecialidadesEnCombo() {
        view.getCmbSelection().removeAllItems();
        view.getCmbSelection().addItem("Select one");
        for (Specialty s : Specialty.values()) view.getCmbSelection().addItem(s.name());
    }

    // Cargar tipos de habitación
    private void cargarTiposDeHabitacion() {
        view.getCmbRoomType().removeAllItems();
        view.getCmbRoomType().addItem("Select one");
        view.getCmbRoomType().addItem("Private");
        view.getCmbRoomType().addItem("Shared");
        view.getCmbRoomType().addItem("Intensive Care");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnLogout()) handleLogout();
        else if (e.getSource() == view.getBtnBack()) handleBack();
        else if (e.getSource() == view.getBtnCreateAppointment()) handleCreateAppointment();
        else if (e.getSource() == view.getBtnSave()) handleUpdateInfo();
        else if (e.getSource() == view.getBtnRefresh()) cargarHistorialCitas();
        else if (e.getSource() == view.getRdoDoctor()) cargarDoctoresEnCombo();
        else if (e.getSource() == view.getRdoSpecialty()) cargarEspecialidadesEnCombo();
        else if (e.getSource() == view.getBtnCancelAppointment()) handleCancelAppointment();
        else if (e.getSource() == view.getBtnCreateHospitalization()) handleCreateHospitalization();
    }

    private void handleUpdateInfo() {
        String fname = view.getTxtFirstname().getText().trim();
        String lname = view.getTxtLastname().getText().trim();
        String bdate = view.getTxtBirthdate().getText().trim();
        String genderStr = (String) view.getCmbGender().getSelectedItem();
        String email = view.getTxtEmail().getText().trim();
        String phoneStr = view.getTxtPhone().getText().trim();
        String address = view.getTxtAddress().getText().trim();
        String user = view.getTxtUsername().getText().trim();
        String pass = view.getTxtPassword().getText();
        String conf = view.getTxtPasswordConfirm().getText();

        Response r = actualizarPacienteLogica(fname, lname, bdate, genderStr, email, phoneStr, address, user, pass, conf);
        
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            view.getTxtPassword().setText("");
            view.getTxtPasswordConfirm().setText("");
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Response actualizarPacienteLogica(String fname, String lname, String bdate, String genderStr, String email, String phoneStr, String address, String user, String pass, String conf) {
        if (fname.isEmpty() || lname.isEmpty() || bdate.isEmpty() || genderStr == null || genderStr.equals("Select one") || email.isEmpty() || phoneStr.isEmpty() || address.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            return new Response(400, "Por favor complete todos los campos.");
        }
        
        if (!pass.equals(conf)) return new Response(400, "Las contraseñas no coinciden.");
        if (!phoneStr.matches("\\d{10}")) return new Response(400, "El teléfono debe tener exactamente 10 dígitos.");
        if (!email.matches(".+@.+\\.com")) return new Response(400, "Email inválido. Debe seguir el formato XXXXX@XXXXX.com");

        LocalDate bd;
        try {
            bd = LocalDate.parse(bdate);
        } catch (Exception e) {
            return new Response(400, "Formato de fecha de nacimiento inválido. Use AAAA-MM-DD.");
        }

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();
        
        for (User u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(user) && u.getId() != pacienteLogueado.getId()) {
                return new Response(400, "El nombre de usuario ya está en uso. Elija otro.");
            }
        }

        for (User u : usuarios) {
            if (u.getId() == pacienteLogueado.getId()) {
                Patient p = (Patient) u;
                p.setFirstname(fname);
                p.setLastname(lname);
                p.setBirthdate(bd);
                p.setGender(genderStr.equals("Female"));
                p.setEmail(email);
                p.setPhone(Long.parseLong(phoneStr));
                p.setAddress(address);
                p.setUsername(user);
                p.setPassword(pass);
                
                pacienteLogueado.setFirstname(fname);
                pacienteLogueado.setLastname(lname);
                pacienteLogueado.setBirthdate(bd);
                pacienteLogueado.setUsername(user);
                break;
            }
        }
        
        JsonManager.guardarUsuarios(usuarios);
        return new Response(200, "Información actualizada exitosamente.");
    }
    
    private void handleCreateHospitalization() {
        String reason = view.getTxaHospitalizationReason().getText().trim();
        String doctorSel = (String) view.getCmbAttendingDoctor().getSelectedItem();
        String dateStr = view.getTxtAdmissionDate().getText().trim();
        String roomType = (String) view.getCmbRoomType().getSelectedItem();
        String obs = view.getTxaObservations().getText().trim();

        Response r = solicitarHospitalizacionLogica(reason, doctorSel, dateStr, roomType, obs);
        
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            view.getTxaHospitalizationReason().setText(""); view.getTxtAdmissionDate().setText("");
            view.getTxaObservations().setText(""); view.getCmbAttendingDoctor().setSelectedIndex(0);
            view.getCmbRoomType().setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Response solicitarHospitalizacionLogica(String reason, String doctorSel, String dateStr, String roomType, String obs) {
        if (reason.isEmpty() || doctorSel == null || doctorSel.equals("Select one") || dateStr.isEmpty() || roomType == null || roomType.equals("Select one")) {
            return new Response(400, "Por favor complete todos los campos obligatorios.");
        }
        try {
            LocalDate admissionDate = LocalDate.parse(dateStr);
            Doctor doctorAsignado = null;
            for (User u : JsonManager.cargarUsuarios()) {
                if (u instanceof Doctor) {
                    Doctor d = (Doctor) u;
                    if ((d.getFirstname() + " " + d.getLastname() + " - " + d.getSpecialty().name()).equals(doctorSel)) {
                        doctorAsignado = d;
                        break;
                    }
                }
            }
            int numHosp = (pacienteLogueado.getHospitalizations() != null) ? pacienteLogueado.getHospitalizations().size() : 0;
            String nuevoId = String.format("H-%d-%04d", pacienteLogueado.getId(), numHosp);
            Hospitalization nuevaHosp = new Hospitalization(nuevoId, pacienteLogueado, doctorAsignado, admissionDate, reason, roomType, obs);
            
            // Iniciar siempre en REQUESTED
            nuevaHosp.setStatus(HospitalizationStatus.REQUESTED); 
            
            ArrayList<User> usuarios = JsonManager.cargarUsuarios();
            for (User u : usuarios) {
                if (u.getId() == pacienteLogueado.getId()) {
                    if (((Patient) u).getHospitalizations() == null) ((Patient) u).setHospitalizations(new ArrayList<>());
                    ((Patient) u).getHospitalizations().add(nuevaHosp);
                    break;
                }
            }
            JsonManager.guardarUsuarios(usuarios);
            return new Response(200, "Hospitalización solicitada exitosamente.");
        } catch (java.time.format.DateTimeParseException e) {
            return new Response(400, "Formato de fecha inválido. Use AAAA-MM-DD.");
        }
    }
    
    private void handleCancelAppointment() {
        String idSeleccionado = (String) view.getCmbCancelAppointmentId().getSelectedItem();
        if (idSeleccionado == null || idSeleccionado.equals("Select one")) {
            JOptionPane.showMessageDialog(view, "Seleccione una cita válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Response r = appointmentController.cancelarCita(idSeleccionado, pacienteLogueado.getId());

        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            // La tabla se actualiza automáticamente vía patrón observador
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCreateAppointment() {
        String dateStr = view.getTxtAppointmentDate().getText();
        String timeStr = view.getTxtAppointmentTime().getText();
        String reason = view.getTxaAppointmentReason().getText();
        String typeStr = (String) view.getCmbAppointmentType().getSelectedItem();
        String selection = (String) view.getCmbSelection().getSelectedItem();

        Response r = solicitarCitaLogica(dateStr, timeStr, reason, typeStr, selection);
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            // La tabla se actualiza automáticamente vía patrón observador
            view.getTxtAppointmentDate().setText("");
            view.getTxtAppointmentTime().setText("");
            view.getTxaAppointmentReason().setText("");
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Asignación por disponibilidad
    public Response solicitarCitaLogica(String dateStr, String timeStr, String reason, String typeStr, String selection) {
        if (dateStr.isEmpty() || timeStr.isEmpty() || reason.isEmpty() || selection == null || selection.equals("Select one")) {
            return new Response(400, "Por favor complete todos los campos.");
        }

        boolean isRemote = typeStr.equals("Remote");

        // Delegar al AppointmentController según el tipo de selección
        // SRP: PatientController no conoce la lógica de disponibilidad de doctores
        if (view.getRdoDoctor().isSelected()) {
            return appointmentController.solicitarCitaPorDoctor(
                    pacienteLogueado.getId(), selection, dateStr, timeStr, reason, isRemote);
        } else {
            return appointmentController.solicitarCitaPorEspecialidad(
                    pacienteLogueado.getId(), selection, dateStr, timeStr, reason, isRemote);
        }
    }

    private void handleLogout() {
        ModelEventBus.getInstance().unsubscribe(this);
        view.dispose();
        new AuthController(new LoginView());
    }

    private void handleBack() {
        if (isAdmin) {
            ModelEventBus.getInstance().unsubscribe(this);
            view.dispose();
            new AdminController(new AdminView());
        }
    }
}