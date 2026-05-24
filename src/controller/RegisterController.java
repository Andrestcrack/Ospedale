package controller;

import view.LoginView;
import model.Patient;
import model.User;
import persistence.JsonManager;
import utils.Response;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;

public class RegisterController implements ActionListener {
    
    private LoginView view;

    public RegisterController(LoginView view) {
        this.view = view;
        initListeners();
    }

    private void initListeners() {
        this.view.getBtnRegisterPatient().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnRegisterPatient()) {
            handleRegister();
        }
    }

    private void handleRegister() {
        String idStr = view.getTxtId().getText().trim();
        String fname = view.getTxtFirstname().getText().trim();
        String lname = view.getTxtLastname().getText().trim();
        String bdate = view.getTxtBirthdate().getText().trim();
        String genderStr = (String) view.getCmbGender().getSelectedItem();
        String email = view.getTxtEmail().getText().trim();
        String phoneStr = view.getTxtPhone().getText().trim();
        String address = view.getTxtAddress().getText().trim();
        String user = view.getTxtRegUsername().getText().trim();
        String pass = view.getTxtRegPassword().getText();
        String conf = view.getTxtRegPasswordConfirm().getText();

        Response r = registrarPacienteLogica(idStr, fname, lname, bdate, genderStr, email, phoneStr, address, user, pass, conf);
        
        if (r.getStatusCode() == 200) {
            JOptionPane.showMessageDialog(view, r.getMessage());
            limpiarCamposRegistro();
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Response registrarPacienteLogica(String idStr, String fname, String lname, String bdate, String genderStr, String email, String phoneStr, String address, String user, String pass, String conf) {
        
        if (idStr.isEmpty() || fname.isEmpty() || lname.isEmpty() || bdate.isEmpty() || genderStr == null || genderStr.equals("Select one") || email.isEmpty() || phoneStr.isEmpty() || address.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            return new Response(400, "Por favor complete todos los campos.");
        }
        
        if (!pass.equals(conf)) return new Response(400, "Las contraseñas no coinciden.");
        if (!idStr.matches("\\d{12}")) return new Response(400, "El ID debe tener exactamente 12 dígitos.");
        if (!phoneStr.matches("\\d{10}")) return new Response(400, "El teléfono debe tener exactamente 10 dígitos.");
        if (!email.matches(".+@.+\\.com")) return new Response(400, "Email inválido. Formato correcto: XXXXX@XXXXX.com");

        long id;
        try {
            id = Long.parseLong(idStr);
            if (id <= 0) return new Response(400, "El ID debe ser mayor a 0.");
        } catch (NumberFormatException e) {
            return new Response(400, "El ID ingresado es inválido.");
        }

        LocalDate bd;
        try {
            bd = LocalDate.parse(bdate);
        } catch (Exception e) {
            return new Response(400, "Formato de fecha de nacimiento inválido. Use AAAA-MM-DD.");
        }

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();
        
        for (User u : usuarios) {
            if (u.getId() == id) {
                return new Response(400, "El ID ya se encuentra registrado en el sistema.");
            }
            if (u.getUsername().equalsIgnoreCase(user)) {
                return new Response(400, "El nombre de usuario ya está en uso. Por favor elija otro.");
            }
        }

        boolean isFemale = genderStr.equals("Female");
        long phone = Long.parseLong(phoneStr);

        // ====================================================================================
        // CORRECCIÓN FINAL: Orden exacto del constructor de Patient.java enviando 'bd' y 'phone'
        // ====================================================================================
        Patient newPatient = new Patient(id, user, fname, lname, pass, email, bd, isFemale, phone, address);
        
        usuarios.add(newPatient);
        JsonManager.guardarUsuarios(usuarios);
        
        return new Response(200, "¡Paciente registrado exitosamente! Ya puede iniciar sesión.");
    }

    private void limpiarCamposRegistro() {
        view.getTxtId().setText("");
        view.getTxtFirstname().setText("");
        view.getTxtLastname().setText("");
        view.getTxtBirthdate().setText("");
        view.getCmbGender().setSelectedIndex(0);
        view.getTxtEmail().setText("");
        view.getTxtPhone().setText("");
        view.getTxtAddress().setText("");
        view.getTxtRegUsername().setText("");
        view.getTxtRegPassword().setText("");
        view.getTxtRegPasswordConfirm().setText("");
    }
}