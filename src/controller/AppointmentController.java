package controller;

import model.*;
import persistence.JsonManager;
import utils.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class AppointmentController {

    public Response solicitarCitaPorDoctor(long patientId, String doctorSel, String dateStr, String timeStr, String reason, boolean isRemote) {

        try {

            ArrayList<User> usuarios = JsonManager.cargarUsuarios();

            Patient paciente = null;
            Doctor doctor = null;

            for (User u : usuarios) {

                if (u instanceof Patient && u.getId() == patientId) paciente = (Patient) u;

                if (u instanceof Doctor) {

                    Doctor d = (Doctor) u;

                    String info = d.getFirstname() + " " + d.getLastname() + " - " + d.getSpecialty().name();

                    if (info.equals(doctorSel)) doctor = d;
                }
            }

            if (paciente == null || doctor == null)
                return new Response(404, "Paciente o doctor no encontrado.");

            LocalDateTime fecha = LocalDateTime.parse(dateStr + "T" + timeStr);

            int num = (paciente.getAppointments() == null) ? 0 : paciente.getAppointments().size();

            String id = String.format("A-%d-%04d", patientId, num);

            Appointment cita = new Appointment(id, paciente, doctor, doctor.getSpecialty(), fecha, reason, isRemote);

            cita.setStatus(AppointmentStatus.REQUESTED);

            paciente.getAppointments().add(cita);

            JsonManager.guardarUsuarios(usuarios);

            return new Response(200, "Cita solicitada.");

        } catch (Exception e) {

            return new Response(400, e.getMessage());
        }
    }

    public Response solicitarCitaPorEspecialidad(long patientId, String specialty, String dateStr, String timeStr, String reason, boolean isRemote) {

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios) {

            if (u instanceof Doctor) {

                Doctor d = (Doctor) u;

                if (d.getSpecialty().name().equals(specialty.toUpperCase().replace(" ", "_"))) {

                    String info = d.getFirstname() + " " + d.getLastname() + " - " + d.getSpecialty().name();

                    return solicitarCitaPorDoctor(patientId, info, dateStr, timeStr, reason, isRemote);
                }
            }
        }

        return new Response(404, "No hay doctor disponible.");
    }

    public Response aceptarCita(String id, long doctorId) {

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios)
            if (u instanceof Patient)
                for (Appointment a : ((Patient) u).getAppointments())
                    if (a.getId().equals(id)) {

                        a.setStatus(AppointmentStatus.PENDING);

                        JsonManager.guardarUsuarios(usuarios);

                        return new Response(200, "Cita aceptada.");
                    }

        return new Response(404, "No encontrada.");
    }

    public Response cancelarCita(String id, long patientId) {

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios)
            if (u instanceof Patient && u.getId() == patientId)
                for (Appointment a : ((Patient) u).getAppointments())
                    if (a.getId().equals(id)) {

                        a.setStatus(AppointmentStatus.CANCELED);

                        JsonManager.guardarUsuarios(usuarios);

                        return new Response(200, "Cita cancelada.");
                    }

        return new Response(404, "No encontrada.");
    }

    public Response completarCita(String id, long doctorId, String diagnosis, String observations, String treatment, String followUp) {

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios)
            if (u instanceof Patient)
                for (Appointment a : ((Patient) u).getAppointments())
                    if (a.getId().equals(id)) {

                        a.setDiagnosis(diagnosis);
                        a.setObservations(observations);
                        a.setRecommendedTreatment(treatment);
                        a.setFollowUp(followUp);

                        a.setStatus(AppointmentStatus.COMPLETED);

                        JsonManager.guardarUsuarios(usuarios);

                        return new Response(200, "Cita completada.");
                    }

        return new Response(404, "No encontrada.");
    }

    public Response reagendarCita(String id, long doctorId, String newTime, String reason) {

        return new Response(200, "Cita reprogramada.");
    }

    public Response prescribirMedicamentos(String id, long doctorId, ArrayList<Prescription> meds) {

        return new Response(200, "Medicamentos agregados.");
    }
}