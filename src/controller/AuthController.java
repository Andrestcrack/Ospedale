package controller;

import java.util.List;

import model.User;
import response.Response;
import response.StatusCode;
/**
 *
 * @author msand
 */
public class AuthController {

    public Response<User> login(String username, String password, List<User> users) {

        try {

            if (username == null || password == null
                    || username.isBlank()
                    || password.isBlank()) {

                return new Response<>(StatusCode.BAD_REQUEST,
                        "Usuario o contraseña vacíos",
                        null);
            }

            for (User user : users) {

                if (user.getUsername().equals(username)
                        && user.getPassword().equals(password)) {

                    return new Response<>(StatusCode.OK,
                            "Inicio de sesión exitoso",
                            user);
                }
            }

            return new Response<>(StatusCode.UNAUTHORIZED,
                    "Credenciales incorrectas",
                    null);

        } catch (Exception e) {

            return new Response<>(StatusCode.INTERNAL_ERROR,
                    e.getMessage(),
                    null);
        }
    }
}