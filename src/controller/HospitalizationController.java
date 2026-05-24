package controller;

import model.*;
import persistence.JsonManager;
import response.Response;

import java.util.ArrayList;
import java.time.LocalDate;

public class HospitalizationController {

    public Response aprobarHospitalizacion(String idHosp) {

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios) {
            if (u instanceof Patient && ((Patient) u).getHospitalizations() != null) {
                for (Hospitalization h : ((Patient) u).getHospitalizations()) {
                    if (h.getId().equals(idHosp)) {

                        h.setStatus(HospitalizationStatus.ONGOING);

                        JsonManager.guardarUsuarios(usuarios);

                        return new Response(200, "Hospitalización aprobada.");
                    }
                }
            }
        }

        return new Response(404, "No encontrada.");
    }

    public Response cancelarHospitalizacion(String idHosp) {

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios) {
            if (u instanceof Patient && ((Patient) u).getHospitalizations() != null) {
                for (Hospitalization h : ((Patient) u).getHospitalizations()) {
                    if (h.getId().equals(idHosp)) {

                        h.setStatus(HospitalizationStatus.CANCELED);

                        JsonManager.guardarUsuarios(usuarios);

                        return new Response(200, "Hospitalización cancelada.");
                    }
                }
            }
        }

        return new Response(404, "No encontrada.");
    }

    public Response hospitalizacionDirecta(String idCita, long doctorId, String dateStr, String duration, String reason, String obs) {

        if (idCita == null || idCita.equals("Select one") || dateStr.isEmpty() || reason.isEmpty()) {
            return new Response(400, "Faltan datos.");
        }

        try {

            LocalDate admissionDate = LocalDate.parse(dateStr);

            ArrayList<User> usuarios = JsonManager.cargarUsuarios();

            for (User u : usuarios) {
                if (u instanceof Patient) {

                    Patient p = (Patient) u;

                    for (Appointment a : p.getAppointments()) {
                        if (a.getId().equals(idCita)) {

                            a.setStatus(AppointmentStatus.COMPLETED);

                            String nuevoId = String.format("H-%d-%04d",
                                    p.getId(),
                                    (p.getHospitalizations() != null)
                                    ? p.getHospitalizations().size()
                                    : 0);

                            Hospitalization nuevaHosp = new Hospitalization(
                                    nuevoId,
                                    p,
                                    a.getDoctor(),
                                    admissionDate,
                                    reason,
                                    "Direct Admission",
                                    obs);

                            nuevaHosp.setStatus(HospitalizationStatus.ONGOING);

                            nuevaHosp.setDuration(duration);

                            if (p.getHospitalizations() == null) {
                                p.setHospitalizations(new ArrayList<>());
                            }

                            p.getHospitalizations().add(nuevaHosp);

                            JsonManager.guardarUsuarios(usuarios);

                            return new Response(200, "Hospitalizado. Cita COMPLETED.");
                        }
                    }
                }
            }

            return new Response(404, "Cita no encontrada.");

        } catch (java.time.format.DateTimeParseException ex) {

            return new Response(400, "Fecha inválida (AAAA-MM-DD).");
        }
    }
}
