package com.lugares.api.service;

import com.lugares.api.entity.FcmToken;
import com.lugares.api.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    public void registrar(FcmToken fcmToken) {
        fcmTokenRepository.findByToken(fcmToken.getToken())
                .ifPresentOrElse(
                        existing -> { /* token ya existe, no duplicar */ },
                        () -> fcmTokenRepository.save(fcmToken)
                );
    }
}
