package controller;

import com.formdev.flatlaf.FlatDarkLaf;
import view.LoginView;
import javax.swing.UIManager;

public class Main {
    
    public static void main(String[] args) {
        // Configuramos el diseño oscuro (FlatLaf)
        System.setProperty("flatlaf.useNativeLibrary", "false");
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Error al inicializar el Look and Feel");
        }

        // Iniciamos el sistema a través del patrón MVC
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Instanciamos la vista principal
                LoginView loginWindow = new LoginView();
                
                // Le entregamos el control a los Controladores
                new AuthController(loginWindow); 
                new RegisterController(loginWindow); 
            }
        });
    }
}