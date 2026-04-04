package com.lugares.api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${FIREBASE_CREDENTIALS:}")
    private String firebaseCredentials;

    @PostConstruct
    public void initializeFirebase() {
        if (firebaseCredentials == null || firebaseCredentials.isBlank()) {
            log.warn("FIREBASE_CREDENTIALS no configurada — Firebase deshabilitado");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            String json = decodeCredentials(firebaseCredentials);
            InputStream credentialsStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase inicializado correctamente");
        } catch (IOException e) {
            log.error("Error al inicializar Firebase", e);
        }
    }

    private String decodeCredentials(String credentials) {
        if (credentials.trim().startsWith("{")) {
            return credentials;
        }
        return new String(Base64.getDecoder().decode(credentials), StandardCharsets.UTF_8);
    }
}
