package com.piedraazul.appointment.service;

import com.piedraazul.appointment.model.Appointment;
import com.piedraazul.appointment.repository.AppointmentRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String AUDIT_EXCHANGE = "audit.exchange";
    private static final String AUDIT_ROUTING_KEY = "audit.routing.key";

    public Appointment createAppointment(Appointment appointment) {
        // Validación de no colisión: "el sistema detecta conflicto, rechaza operación"
        Optional<Appointment> existing = appointmentRepository.findByProfessionalIdAndAppointmentTime(
                appointment.getProfessionalId(), appointment.getAppointmentTime());
        
        if (existing.isPresent() && !"CANCELLED".equals(existing.get().getStatus())) {
            throw new RuntimeException("Horario no disponible");
        }

        appointment.setStatus("SCHEDULED");
        Appointment saved = appointmentRepository.save(appointment);

        // Envío de evento de auditoría
        String auditMessage = String.format("Cita agendada: Paciente %d, Profesional %d, Fecha %s",
                saved.getPatientId(), saved.getProfessionalId(), saved.getAppointmentTime().toString());
        sendAudit(auditMessage);

        return saved;
    }

    public Appointment cancelAppointment(Long appointmentId) {
        Appointment existing = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        existing.setStatus("CANCELLED");
        Appointment updated = appointmentRepository.save(existing);

        String auditMessage = String.format("Cita cancelada: CitaId %d", updated.getId());
        sendAudit(auditMessage);

        return updated;
    }

    private void sendAudit(String message) {
        try {
            rabbitTemplate.convertAndSend(AUDIT_EXCHANGE, AUDIT_ROUTING_KEY, message);
        } catch (Exception e) {
            System.err.println("Error al enviar auditoría: " + e.getMessage());
        }
    }
}
