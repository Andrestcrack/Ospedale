package controller;

import utils.Response;
import view.AdminView;
import view.DoctorView;
import view.PatientView;
import view.LoginView;
import model.User;
import model.Doctor;
import model.Patient;
import model.Specialty;
import persistence.JsonManager;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AdminController implements ActionListener {

    private AdminView view;

    public AdminController(AdminView view) {
        this.view = view;
        initListeners();
        cargarCombosDeUsuarios();
        this.view.setVisible(true);
    }

    private void initListeners() {
        this.view.getBtnLogout().addActionListener(this);
        this.view.getBtnRegisterDoctor().addActionListener(this);
        this.view.getBtnGoToDoctorView().addActionListener(this);
        this.view.getjButton3().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnLogout()) handleLogout();
        else if (e.getSource() == view.getBtnRegisterDoctor()) handleRegisterDoctor();
        else if (e.getSource() == view.getBtnGoToDoctorView()) handleGoToDoctorView();
        else if (e.getSource() == view.getjButton3()) handleGoToPatientView();
    }

    private void cargarCombosDeUsuarios() {
        view.getCmbDoctorSelect().removeAllItems();
        view.getjComboBox3().removeAllItems();
        view.getCmbDoctorSelect().addItem("Select one");
        view.getjComboBox3().addItem("Select one");

        for (User u : JsonManager.cargarUsuarios()) {
            if (u instanceof Doctor) view.getCmbDoctorSelect().addItem(u.getId() + " - " + u.getFirstname());
            else if (u instanceof Patient) view.getjComboBox3().addItem(u.getId() + " - " + u.getFirstname());
        }
    }

    private void handleLogout() {
        view.dispose();
        new AuthController(new LoginView());
    }

    private void handleGoToPatientView() {
        String sel = (String) view.getjComboBox3().getSelectedItem();
        if (sel == null || sel.equals("Select one")) return;
        
        Patient p = (Patient) buscarUsuarioPorId(Long.parseLong(sel.split(" - ")[0]));
        if (p != null) {
            view.dispose();
            // 'true' habilita el botón Back para el Admin
            new PatientController(new PatientView(), p, true);
        }
    }

    private void handleGoToDoctorView() {
        String sel = (String) view.getCmbDoctorSelect().getSelectedItem();
        if (sel == null || sel.equals("Select one")) return;
        
        Doctor doc = (Doctor) buscarUsuarioPorId(Long.parseLong(sel.split(" - ")[0]));
        if (doc != null) {
            view.dispose();
            // Pasamos 'true' para habilitar el botón Back, asumiendo que tienes el constructor actualizado en DoctorController
            new DoctorController(new DoctorView(), doc, true);
        }
    }

    private User buscarUsuarioPorId(long id) {
        for (User u : JsonManager.cargarUsuarios()) {
            if (u.getId() == id) return u;
        }
        return null;
    }

    private void handleRegisterDoctor() {
        String f = view.getTxtFirstname().getText().trim();
        String l = view.getTxtLastname().getText().trim();
        String idT = view.getTxtId().getText().trim();
        String s = (String) view.getCmbSpecialty().getSelectedItem();
        String lic = view.getTxtLicenseNumber().getText().trim();
        String off = view.getTxtAssignedOffice().getText().trim();
        String user = view.getTxtUsername().getText().trim();
        String pass = view.getTxtPassword().getText();
        String conf = view.getTxtPasswordConfirm().getText();

        Response r = registrarDoctorLogica(f, l, idT, s, lic, off, user, pass, conf);
        
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            limpiarCamposDoctor();
            cargarCombosDeUsuarios();
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Response registrarDoctorLogica(String f, String l, String idT, String s, String lic, String off, String user, String pass, String conf) {
        // Validación de campos vacíos
        if (f.isEmpty() || l.isEmpty() || idT.isEmpty() || s == null || s.equals("Select one") || lic.isEmpty() || off.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            return new Response(400, "Por favor complete todos los campos.");
        }

        // Validaciones de formato exigidas en el parcial
        if (!pass.equals(conf)) return new Response(400, "Las contraseñas no coinciden.");
        if (!idT.matches("\\d{12}")) return new Response(400, "El ID debe tener exactamente 12 dígitos.");
        if (!lic.matches("L-\\d{10} MTL")) return new Response(400, "Licencia inválida. Formato: L-XXXXXXXXXX MTL.");
        if (!off.matches("O-\\d{3}")) return new Response(400, "Oficina inválida. Formato: O-XXX.");

        ArrayList<User> us = JsonManager.cargarUsuarios();
        long newId = Long.parseLong(idT);

        // Validación de ID mayor a 0
        if (newId <= 0) return new Response(400, "El ID debe ser mayor a 0.");

        // Validación de unicidad para ID y Username
        for (User u : us) {
            if (u.getId() == newId) {
                return new Response(400, "El ID ingresado ya existe en el sistema.");
            }
            if (u.getUsername().equalsIgnoreCase(user)) {
                return new Response(400, "El nombre de usuario ya está en uso. Elija otro.");
            }
        }

        try {
            Specialty spec = Specialty.valueOf(s.toUpperCase().replace(" ", "_").replace("&", "AND"));
            us.add(new Doctor(newId, user, f, l, pass, spec, lic, off));
            JsonManager.guardarUsuarios(us);
            return new Response(200, "¡Doctor registrado exitosamente!");
        } catch (Exception e) {
            return new Response(500, "Error interno al registrar el doctor.");
        }
    }

    private void limpiarCamposDoctor() {
        view.getTxtFirstname().setText(""); 
        view.getTxtLastname().setText(""); 
        view.getTxtId().setText("");
        view.getCmbSpecialty().setSelectedIndex(0); 
        view.getTxtLicenseNumber().setText("");
        view.getTxtAssignedOffice().setText(""); 
        view.getTxtUsername().setText("");
        view.getTxtPassword().setText(""); 
        view.getTxtPasswordConfirm().setText("");
    }
}