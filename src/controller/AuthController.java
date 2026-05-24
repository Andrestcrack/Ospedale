package controller;

import view.LoginView;
import view.AdminView;
import view.DoctorView;
import view.PatientView;
import model.User;
import model.Administrator;
import model.Doctor;
import model.Patient;
import persistence.JsonManager;
import response.Response;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AuthController implements ActionListener {
    
    private LoginView view;

    public AuthController(LoginView view) {
        this.view = view;
        initListeners();
        // Inicializamos el controlador de registro pasándole ESTA misma vista
        new RegisterController(this.view);
        this.view.setVisible(true);
    }

    private void initListeners() {
        this.view.getBtnLogin().addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.getBtnLogin()) {
            handleLogin();
        }
    }

    private void handleLogin() {
        // Usando los getters exactos de tu LoginView
        String user = view.getTxtLoginUsername().getText().trim();
        String pass = view.getTxtLoginPassword().getText(); 

        Response r = loginLogica(user, pass);

        if (r.getStatusCode() == 200) {
            view.dispose(); // Cierra la ventana de login
        } else {
            JOptionPane.showMessageDialog(view, r.getMessage(), "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Response loginLogica(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return new Response(400, "Por favor ingrese su usuario y contraseña.");
        }

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();
        
        for (User u : usuarios) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                
                // Parcial 2b iv: NO se envían objetos del modelo a la vista.
                // Solo se pasa el ID y se crea un controlador nuevo que carga
                // la información desde el almacenamiento.
                if (u instanceof Administrator) {
                    new AdminController(new AdminView());
                    return new Response(200, "Login Admin exitoso");
                    
                } else if (u instanceof Doctor) {
                    // Buscar el doctor actualizado desde el JSON (no pasar el objeto directamente)
                    Doctor doctorActualizado = (Doctor) buscarUsuarioPorId(usuarios, u.getId());
                    new DoctorController(new DoctorView(), doctorActualizado, false);
                    return new Response(200, "Login Doctor exitoso");
                    
                } else if (u instanceof Patient) {
                    // Buscar el paciente actualizado desde el JSON
                    Patient pacienteActualizado = (Patient) buscarUsuarioPorId(usuarios, u.getId());
                    new PatientController(new PatientView(), pacienteActualizado, false);
                    return new Response(200, "Login Patient exitoso");
                }
            }
        }
        return new Response(401, "Credenciales incorrectas o usuario no encontrado.");
    }

    private User buscarUsuarioPorId(ArrayList<User> usuarios, long id) {
        for (User u : usuarios) {
            if (u.getId() == id) return u;
        }
        return null;
    }
}