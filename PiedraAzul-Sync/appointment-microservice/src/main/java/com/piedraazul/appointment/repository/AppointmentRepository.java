package com.piedraazul.appointment.repository;

import com.piedraazul.appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByProfessionalIdAndAppointmentTimeBetween(Long professionalId, LocalDateTime start, LocalDateTime end);
    Optional<Appointment> findByProfessionalIdAndAppointmentTime(Long professionalId, LocalDateTime appointmentTime);
}
