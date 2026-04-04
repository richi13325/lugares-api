package com.lugares.api.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.lugares.api.entity.FcmToken;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final FcmTokenRepository fcmTokenRepository;

    public void enviarACliente(Long clienteId, String titulo, String mensaje)
            throws FirebaseMessagingException {
        List<FcmToken> tokens = fcmTokenRepository.findByIdCliente(clienteId);

        if (tokens.isEmpty()) {
            throw new BusinessRuleException("El cliente no tiene tokens FCM registrados");
        }

        Notification notification = Notification.builder()
                .setTitle(titulo)
                .setBody(mensaje)
                .build();

        for (FcmToken fcmToken : tokens) {
            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken.getToken())
                    .build();

            FirebaseMessaging.getInstance().send(message);
        }
    }
}
