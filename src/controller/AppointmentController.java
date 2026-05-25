package controller;

import model.*;
import persistence.JsonManager;
import response.Response;
import observer.ModelEvent;
import observer.ModelEventBus;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
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

            // Patrón Observador: notificar a todos los observers que hubo un cambio
            ModelEventBus.getInstance().publish(ModelEvent.APPOINTMENT_CHANGED);

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

                        // Patrón Observador
                        ModelEventBus.getInstance().publish(ModelEvent.APPOINTMENT_CHANGED);

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

                        // Patrón Observador
                        ModelEventBus.getInstance().publish(ModelEvent.APPOINTMENT_CHANGED);

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

                        // Patrón Observador
                        ModelEventBus.getInstance().publish(ModelEvent.APPOINTMENT_CHANGED);

                        return new Response(200, "Cita completada.");
                    }

        return new Response(404, "No encontrada.");
    }

    public Response reagendarCita(String id, long doctorId, String newTime, String reason) {

        // Validar formato de hora con cuartos de hora
        if (newTime == null || !newTime.matches("([01]\\d|2[0-3]):(00|15|30|45)")) {
            return new Response(400, "Hora inválida. Use formato hh:mm con minutos en (00, 15, 30, 45).");
        }

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios) {
            if (u instanceof Patient) {
                for (Appointment a : ((Patient) u).getAppointments()) {
                    if (a.getId().equals(id)) {

                        // Verificar que el doctor sea el responsable
                        if (a.getDoctor() == null || a.getDoctor().getId() != doctorId) {
                            return new Response(400, "Este doctor no es el responsable de la cita.");
                        }

                        // Verificar que la cita no esté completada o cancelada
                        if (a.getStatus() == AppointmentStatus.COMPLETED
                                || a.getStatus() == AppointmentStatus.CANCELED) {
                            return new Response(400, "No se puede reagendar una cita COMPLETED o CANCELED.");
                        }

                        // Conservar el mismo día, solo cambiar la hora
                        java.time.LocalDate fechaOriginal = a.getDatetime().toLocalDate();
                        java.time.LocalDateTime nuevaFechaHora;
                        try {
                            nuevaFechaHora = java.time.LocalDateTime.of(
                                    fechaOriginal,
                                    java.time.LocalTime.parse(newTime)
                            );
                        } catch (Exception e) {
                            return new Response(400, "Error al parsear la nueva hora.");
                        }

                        // Verificar disponibilidad en el nuevo horario,
                        // excluyendo la propia cita que se está reagendando
                        if (!isDoctorAvailableExcluding(a.getDoctor(), nuevaFechaHora, id, usuarios)) {
                            return new Response(400, "El doctor ya tiene una cita en ese nuevo horario.");
                        }

                        // Aplicar cambios
                        a.setDatetime(nuevaFechaHora);

                        // Añadir la razón del reagendamiento a la razón original
                        if (reason != null && !reason.isBlank()) {
                            a.setReason(a.getReason() + " | Reagendamiento: " + reason);
                        }

                        JsonManager.guardarUsuarios(usuarios);

                        // Patrón Observador
                        ModelEventBus.getInstance().publish(ModelEvent.APPOINTMENT_CHANGED);

                        return new Response(200, "Cita reagendada para las " + newTime + ".");
                    }
                }
            }
        }

        return new Response(404, "Cita no encontrada.");
    }

    private boolean isDoctorAvailableExcluding(Doctor doctor, java.time.LocalDateTime ldt,
                                               String excludeId, ArrayList<User> usuarios) {
        final int DURACION_MINUTOS = 15;

        for (User u : usuarios) {
            if (u instanceof Patient && ((Patient) u).getAppointments() != null) {
                for (Appointment a : ((Patient) u).getAppointments()) {
                    // Ignorar la cita que se está reagendando
                    if (excludeId != null && a.getId().equals(excludeId)) continue;
                    // Ignorar citas de otros doctores
                    if (a.getDoctor() == null || a.getDoctor().getId() != doctor.getId()) continue;
                    // Ignorar citas canceladas
                    if (a.getStatus() == AppointmentStatus.CANCELED) continue;

                    long diffMinutos = Math.abs(
                        java.time.Duration.between(a.getDatetime(), ldt).toMinutes()
                    );
                    if (diffMinutos < DURACION_MINUTOS) return false;
                }
            }
        }
        return true;
    }

    public Response prescribirMedicamentos(String id, long doctorId, ArrayList<Prescription> meds) {

        if (id == null || id.equals("Select one")) {
            return new Response(400, "Seleccione una cita válida.");
        }
        if (meds == null || meds.isEmpty()) {
            return new Response(400, "Debe agregar al menos un medicamento.");
        }

        ArrayList<User> usuarios = JsonManager.cargarUsuarios();

        for (User u : usuarios) {
            if (u instanceof Patient) {
                for (Appointment a : ((Patient) u).getAppointments()) {
                    if (a.getId().equals(id)) {

                        // Verificar que el doctor sea el responsable
                        if (a.getDoctor() == null || a.getDoctor().getId() != doctorId) {
                            return new Response(400, "Este doctor no es el responsable de la cita.");
                        }

                        // Solo se puede prescribir en citas PENDING
                        if (a.getStatus() != AppointmentStatus.PENDING) {
                            return new Response(400,
                                "Solo se pueden prescribir medicamentos en citas con estado PENDING.");
                        }

                        // Inicializar lista si es null (defensa ante datos legacy del JSON)
                        if (a.getPrescriptions() == null) {
                            a.setPrescriptions(new ArrayList<>());
                        }

                        // Añadir todas las prescripciones una sola vez
                        a.getPrescriptions().addAll(meds);

                        JsonManager.guardarUsuarios(usuarios);

                        // Patrón Observador
                        ModelEventBus.getInstance().publish(ModelEvent.APPOINTMENT_CHANGED);

                        return new Response(200,
                            "Medicamentos prescritos exitosamente (" + meds.size() + " prescripción(es)).");
                    }
                }
            }
        }

        return new Response(404, "Cita no encontrada.");
    }
}