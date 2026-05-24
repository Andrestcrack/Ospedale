package controller;

import view.DoctorView;
import view.AdminView;
import view.LoginView;
import model.*;
import persistence.JsonManager;
import response.Response;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.time.LocalDate;

public class DoctorController implements ActionListener {

    private DoctorView view;
    private Doctor doctorLogueado;
    private boolean isAdmin;

    private final AppointmentController appointmentController = new AppointmentController();

    private final HospitalizationController hospitalizationController = new HospitalizationController();
    
    public DoctorController(DoctorView view, Doctor doctorLogueado, boolean isAdmin) {
        this.view = view;
        this.doctorLogueado = doctorLogueado;
        this.isAdmin = isAdmin;
        
        this.view.getBtnBack().setEnabled(isAdmin);
        initListeners();
        cargarDatosDoctorEnVista();
        cargarCitasYHospitalizaciones();
        this.view.setVisible(true);
    }

    private void initListeners() {
        this.view.getBtnLogout().addActionListener(this);
        this.view.getBtnBack().addActionListener(this);
        this.view.getBtnUpdateInfo().addActionListener(this);
        
        this.view.getBtnAcceptAppointment().addActionListener(this);
        this.view.getBtnCompleteAppointment().addActionListener(this);
        this.view.getBtnRescheduleAppointment().addActionListener(this);
        
        this.view.getBtnAddMedication().addActionListener(this);
        this.view.getBtnPrescribe().addActionListener(this);
        
        this.view.getBtnGenerateHospitalization().addActionListener(this);
        this.view.getBtnCancelHospitalization().addActionListener(this);
        
        // Listener para el botón de buscar historial
        this.view.getBtnSearch().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnLogout()) handleLogout();
        else if (e.getSource() == view.getBtnBack()) handleBack();
        else if (e.getSource() == view.getBtnUpdateInfo()) handleUpdateInfo();
        else if (e.getSource() == view.getBtnAcceptAppointment()) handleAcceptAppointment();
        else if (e.getSource() == view.getBtnCompleteAppointment()) handleCompleteAppointment();
        else if (e.getSource() == view.getBtnRescheduleAppointment()) handleRescheduleAppointment();
        else if (e.getSource() == view.getBtnAddMedication()) handleAddMedication();
        else if (e.getSource() == view.getBtnPrescribe()) handlePrescribe();
        else if (e.getSource() == view.getBtnGenerateHospitalization()) handleGenerateHospitalization();
        else if (e.getSource() == view.getBtnCancelHospitalization()) handleCancelHospitalization();
        // Acción para buscar historial
        else if (e.getSource() == view.getBtnSearch()) handleSearchHistory();
    }

    private void cargarDatosDoctorEnVista() {
        if (doctorLogueado != null) {
            view.getTxtModFirstname().setText(doctorLogueado.getFirstname());
            view.getTxtModLastname().setText(doctorLogueado.getLastname());
            view.getTxtModLicenseNumber().setText(doctorLogueado.getLicenseNumber());
            view.getTxtModAssignedOffice().setText(doctorLogueado.getAssignedOffice());
            view.getTxtModUsername().setText(doctorLogueado.getUsername());
            
            for (int i = 0; i < view.getCmbModSpecialty().getItemCount(); i++) {
                if (view.getCmbModSpecialty().getItemAt(i).toUpperCase().replace(" ", "_").replace("&", "AND").equals(doctorLogueado.getSpecialty().name())) {
                    view.getCmbModSpecialty().setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void handleUpdateInfo() {
        String fname = view.getTxtModFirstname().getText().trim();
        String lname = view.getTxtModLastname().getText().trim();
        String specStr = (String) view.getCmbModSpecialty().getSelectedItem();
        String lic = view.getTxtModLicenseNumber().getText().trim();
        String off = view.getTxtModAssignedOffice().getText().trim();
        String user = view.getTxtModUsername().getText().trim();
        String pass = view.getTxtModPassword().getText();
        String conf = view.getTxtModPasswordConfirm().getText();

        Response r = actualizarDoctorLogica(fname, lname, specStr, lic, off, user, pass, conf);
        
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            view.getTxtModPassword().setText("");
            view.getTxtModPasswordConfirm().setText("");
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Response actualizarDoctorLogica(String fname, String lname, String specStr, String lic, String off, String user, String pass, String conf) {
        if (fname.isEmpty() || lname.isEmpty() || specStr == null || specStr.equals("Select one") || lic.isEmpty() || off.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            return new Response(400, "Por favor complete todos los campos.");
        }
        
        if (!pass.equals(conf)) return new Response(400, "Las contraseñas no coinciden.");
        if (!lic.matches("L-\\d{10} MTL")) return new Response(400, "Licencia inválida. Formato: L-XXXXXXXXXX MTL.");
        if (!off.matches("O-\\d{3}")) return new Response(400, "Oficina inválida. Formato: O-XXX.");

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();
        
        for (User u : usuarios) {
            if (u.getUsername().equalsIgnoreCase(user) && u.getId() != doctorLogueado.getId()) {
                return new Response(400, "El nombre de usuario ya está en uso. Elija otro.");
            }
        }

        for (User u : usuarios) {
            if (u.getId() == doctorLogueado.getId()) {
                Doctor d = (Doctor) u;
                d.setFirstname(fname);
                d.setLastname(lname);
                d.setSpecialty(Specialty.valueOf(specStr.toUpperCase().replace(" ", "_").replace("&", "AND")));
                d.setLicenseNumber(lic);
                d.setAssignedOffice(off);
                d.setUsername(user);
                d.setPassword(pass);
                
                doctorLogueado.setFirstname(fname);
                doctorLogueado.setLastname(lname);
                doctorLogueado.setUsername(user);
                break;
            }
        }
        
        JsonManager.guardarUsuarios(usuarios);
        return new Response(200, "Información del doctor actualizada exitosamente.");
    }

    private void cargarCitasYHospitalizaciones() {
        DefaultTableModel model = (DefaultTableModel) view.getTblAppointments().getModel();
        model.setRowCount(0);
        
        view.getCmbAcceptAppointmentId().removeAllItems(); view.getCmbAcceptAppointmentId().addItem("Select one");
        view.getCmbCompleteAppointment().removeAllItems(); view.getCmbCompleteAppointment().addItem("Select one");
        view.getCmbRescheduleAppointment().removeAllItems(); view.getCmbRescheduleAppointment().addItem("Select one");
        view.getCmbMedicationAppointmentId().removeAllItems(); view.getCmbMedicationAppointmentId().addItem("Select one");
        view.getCmbHospRequests().removeAllItems(); view.getCmbHospRequests().addItem("Select one");
        view.getCmbHospPatientId().removeAllItems(); view.getCmbHospPatientId().addItem("Select one");
        
        // Limpiar combo de historial
        view.getCmbHistoryPatient().removeAllItems(); view.getCmbHistoryPatient().addItem("Select one");

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();
        ArrayList<Appointment> citasDelDoctor = new ArrayList<>();
        
        for (User u : usuarios) {
            if (u instanceof Patient) {
                Patient p = (Patient) u;
                
                // Agregar al paciente al ComboBox de Historial
                view.getCmbHistoryPatient().addItem(String.valueOf(p.getId()));
                
                // Recopilar citas
                if (p.getAppointments() != null) {
                    for (Appointment cita : p.getAppointments()) {
                        if (cita.getDoctor() != null && cita.getDoctor().getId() == doctorLogueado.getId()) {
                            citasDelDoctor.add(cita);
                        }
                    }
                }
                
                // Recopilar hospitalizaciones para combos
                if (p.getHospitalizations() != null) {
                    for (Hospitalization h : p.getHospitalizations()) {
                        if (h.getDoctor() != null && h.getDoctor().getId() == doctorLogueado.getId() && h.getStatus() == HospitalizationStatus.REQUESTED) {
                            view.getCmbHospRequests().addItem(h.getId());
                        }
                    }
                }
            }
        }

        // Ordenamiento estricto descendente (más nuevas primero)
        citasDelDoctor.sort((a, b) -> b.getDatetime().compareTo(a.getDatetime()));

        // Inyectar a la vista en orden correcto
        for(Appointment cita : citasDelDoctor) {
            String patientName = cita.getPatient().getFirstname() + " " + cita.getPatient().getLastname();
            model.addRow(new Object[]{ cita.getId(), cita.getDatetime().toString().replace("T", " "), patientName, cita.getSpecialty() != null ? cita.getSpecialty().name() : "Unassigned", cita.isType() ? "Remote" : "In-person", cita.getStatus() != null ? cita.getStatus().name() : "Unassigned" });
            
            if (cita.getStatus() == AppointmentStatus.REQUESTED) {
                view.getCmbAcceptAppointmentId().addItem(cita.getId());
                view.getCmbRescheduleAppointment().addItem(cita.getId());
            } else if (cita.getStatus() == AppointmentStatus.PENDING) {
                view.getCmbCompleteAppointment().addItem(cita.getId());
                view.getCmbRescheduleAppointment().addItem(cita.getId());
                view.getCmbMedicationAppointmentId().addItem(cita.getId());
                view.getCmbHospPatientId().addItem(cita.getId());
            }
        }
    }
    
    // Método para procesar la búsqueda del historial
    private void handleSearchHistory() {
        String idSeleccionado = (String) view.getCmbHistoryPatient().getSelectedItem();
        if (idSeleccionado == null || idSeleccionado.equals("Select one")) {
            JOptionPane.showMessageDialog(view, "Seleccione un paciente válido.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTblPatientHistory().getModel();
        model.setRowCount(0);

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();
        for (User u : usuarios) {
            if (u instanceof Patient && String.valueOf(u.getId()).equals(idSeleccionado)) {
                Patient p = (Patient) u;
                if (p.getAppointments() != null) {
                    // Se ordenan descendentemente como exige la rúbrica
                    ArrayList<Appointment> citasPaciente = new ArrayList<>(p.getAppointments());
                    citasPaciente.sort((a, b) -> b.getDatetime().compareTo(a.getDatetime()));

                    for (Appointment cita : citasPaciente) {
                        // Mostrar solo las citas que el paciente tuvo con ESTE doctor
                        if (cita.getDoctor() != null && cita.getDoctor().getId() == doctorLogueado.getId()) {
                            model.addRow(new Object[]{
                                cita.getId(), 
                                cita.getDatetime().toString().replace("T", " "), 
                                p.getFirstname() + " " + p.getLastname(), 
                                cita.getSpecialty() != null ? cita.getSpecialty().name() : "Unassigned", 
                                cita.isType() ? "Remote" : "In-person", 
                                cita.getStatus().name()
                            });
                        }
                    }
                }
                break;
            }
        }
    }

    private void handleAcceptAppointment() {
        String idSeleccionado = (String) view.getCmbAcceptAppointmentId().getSelectedItem();
        Response r = aceptarCitaLogica(idSeleccionado);
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            cargarCitasYHospitalizaciones();
        } else JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Response aceptarCitaLogica(String idSeleccionado) {
        if (idSeleccionado == null || idSeleccionado.equals("Select one"))
            return new Response(400, "Seleccione una cita válida.");
        // Delegar: SRP — la lógica de estados de cita pertenece al AppointmentController
        return appointmentController.aceptarCita(idSeleccionado, doctorLogueado.getId());
    }
    
    private void handleCompleteAppointment() {
        String idSeleccionado = (String) view.getCmbCompleteAppointment().getSelectedItem();
        Response r = completarCitaLogica(idSeleccionado);
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            cargarCitasYHospitalizaciones();
        } else JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Response completarCitaLogica(String id) {
        if (id == null || id.equals("Select one")) return new Response(400, "Seleccione una cita válida.");
        // Recoger los campos de completación de la vista
        String diagnosis = view.getTxaDiagnosis().getText().trim();
        String observations = view.getTxaCompleteObservations().getText().trim();
        String treatment = view.getTxaRecommendedTreatment().getText().trim();
        String followUp = view.getTxaFollowUpIndication().getText().trim();
        // Delegar al AppointmentController — corrige el bug original que ponía CANCELED
        return appointmentController.completarCita(id, doctorLogueado.getId(),
                diagnosis, observations, treatment, followUp);
    }

    private void handleRescheduleAppointment() {
        String idSeleccionado = (String) view.getCmbRescheduleAppointment().getSelectedItem();
        String nuevaHora = view.getTxtNewTimeAppointment().getText().trim();
        String razon = view.getTxtRescheduleReason().getText().trim();
        
        Response r = reprogramarCitaLogica(idSeleccionado, nuevaHora, razon);
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            view.getTxtNewTimeAppointment().setText(""); view.getTxtRescheduleReason().setText("");
            cargarCitasYHospitalizaciones();
        } else JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Response reprogramarCitaLogica(String id, String newTime, String reason) {
        if (id == null || id.equals("Select one")) return new Response(400, "Seleccione una cita.");
        // Delegar — la validación de cuartos de hora y disponibilidad está en AppointmentController
        return appointmentController.reagendarCita(id, doctorLogueado.getId(), newTime, reason);
    }

    private void handleAddMedication() {
        String idCita = (String) view.getCmbMedicationAppointmentId().getSelectedItem();
        if (idCita == null || idCita.equals("Select one")) {
            JOptionPane.showMessageDialog(view, "Por favor seleccione una cita PENDING primero.");
            return;
        }
        
        String name = view.getTxtMedicationName().getText().trim();
        String dose = view.getTxtMedicationDose().getText().trim();
        String route = view.getTxtMedicationRoute().getText().trim();
        String freq = view.getTxtMedicationFrequency().getText().trim();
        String duration = view.getTxtMedicationDuration().getText().trim();
        String inst = view.getTxtMedicationInstructions().getText().trim();

        if (name.isEmpty() || dose.isEmpty() || route.isEmpty() || duration.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Por favor complete los campos obligatorios del medicamento.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTblMedications().getModel();
        model.addRow(new Object[]{idCita, name, dose, route, duration, inst, freq});

        view.getTxtMedicationName().setText(""); view.getTxtMedicationDose().setText("");
        view.getTxtMedicationRoute().setText(""); view.getTxtMedicationFrequency().setText("");
        view.getTxtMedicationDuration().setText(""); view.getTxtMedicationInstructions().setText("");
    }

    private void handlePrescribe() {
        String idCita = (String) view.getCmbMedicationAppointmentId().getSelectedItem();
        DefaultTableModel model = (DefaultTableModel) view.getTblMedications().getModel();
        
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Agregue al menos un medicamento a la tabla usando el botón Add.");
            return;
        }

        ArrayList<Prescription> listaPrescripciones = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            listaPrescripciones.add(new Prescription((String) model.getValueAt(i, 1), (String) model.getValueAt(i, 2), (String) model.getValueAt(i, 3), (String) model.getValueAt(i, 6), (String) model.getValueAt(i, 4), (String) model.getValueAt(i, 5)));
        }

        Response r = prescribirMedicamentosLogica(idCita, listaPrescripciones);
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            model.setRowCount(0); 
            view.getCmbMedicationAppointmentId().setSelectedIndex(0);
        } else JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Response prescribirMedicamentosLogica(String idCita, ArrayList<Prescription> prescripciones) {
        // Delegar — la validación del estado PENDING es responsabilidad del AppointmentController
        return appointmentController.prescribirMedicamentos(
                idCita, doctorLogueado.getId(), prescripciones);
    }

    private void handleGenerateHospitalization() {
        if (view.getRdoHospRequests().isSelected()) {
            Response r = aprobarHospitalizacionLogica((String) view.getCmbHospRequests().getSelectedItem());
            if (r.getStatusCode() == 200) { JOptionPane.showMessageDialog(view, r.getMessage()); cargarCitasYHospitalizaciones(); } 
            else JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else if (view.getRdoHospPatientId().isSelected()) {
            Response r = hospitalizacionDirectaLogica((String) view.getCmbHospPatientId().getSelectedItem(), view.getTxtHospDateEntry().getText().trim(), view.getTxtHospDuration().getText().trim(), view.getTxaHospReason().getText().trim(), view.getTxaHospObservations().getText().trim());
            if (r.getStatusCode() == 200) {
                JOptionPane.showMessageDialog(view, r.getMessage());
                view.getTxtHospDateEntry().setText(""); view.getTxtHospDuration().setText(""); view.getTxaHospReason().setText(""); view.getTxaHospObservations().setText("");
                cargarCitasYHospitalizaciones();
            } else JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else JOptionPane.showMessageDialog(view, "Seleccione 'Requests' o 'Patient ID' (Cita).", "Error", JOptionPane.WARNING_MESSAGE);
    }

    private void handleCancelHospitalization() {
        if (view.getRdoHospRequests().isSelected()) {
            Response r = denegarHospitalizacionLogica((String) view.getCmbHospRequests().getSelectedItem());
            if (r.getStatusCode() == 200) { JOptionPane.showMessageDialog(view, r.getMessage()); cargarCitasYHospitalizaciones(); } 
            else JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else JOptionPane.showMessageDialog(view, "Solo se pueden cancelar solicitudes pendientes.", "Error", JOptionPane.WARNING_MESSAGE);
    }

    public Response aprobarHospitalizacionLogica(String idHosp) {

    if (idHosp == null || idHosp.equals("Select one"))
        return new Response(400, "Seleccione una solicitud válida.");

    return hospitalizationController.aprobarHospitalizacion(idHosp);
}

    public Response denegarHospitalizacionLogica(String idHosp) {

    if (idHosp == null || idHosp.equals("Select one"))
        return new Response(400, "Seleccione una solicitud válida.");

    return hospitalizationController.cancelarHospitalizacion(idHosp);
}

    public Response hospitalizacionDirectaLogica(String idCita, String dateStr, String duration, String reason, String obs) {

    return hospitalizationController.hospitalizacionDirecta(idCita, doctorLogueado.getId(), dateStr, duration, reason, obs);
}

    private void handleLogout() { view.dispose(); new AuthController(new LoginView()); }
    private void handleBack() { if (isAdmin) { view.dispose(); new AdminController(new AdminView()); } }
}