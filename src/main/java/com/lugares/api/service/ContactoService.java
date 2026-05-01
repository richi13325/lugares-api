package com.lugares.api.service;

import com.lugares.api.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Value("${app.email.contacto}")
    private String destinatario;

    public void enviarContacto(String nombre, String correo, String asunto, String mensaje) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(destinatario);
            email.setFrom(remitente);
            email.setSubject("Nuevo mensaje de contacto: " + asunto);
            email.setText(String.format("""
                Nuevo mensaje de contacto:

                Nombre: %s
                Correo: %s
                Asunto: %s

                Mensaje:
                %s
                """, nombre, correo, asunto, mensaje));
            mailSender.send(email);
        } catch (MailException e) {
            throw new BusinessRuleException("No se pudo enviar el email. Intentelo mas tarde.");
        }
    }
}
