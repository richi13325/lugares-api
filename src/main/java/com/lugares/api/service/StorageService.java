package com.lugares.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public StorageService(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(120));
        this.webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    // Sube un archivo y devuelve el path relativo (ej: "capsulas/uuid.jpg").
    // Guardá este path en la BD, no la URL completa.
    public String uploadFile(MultipartFile file, String carpeta) {
        try {
            String filePath = carpeta.toLowerCase() + "/" + buildUniqueFileName(file.getOriginalFilename());
            byte[] bytes = file.getBytes();

            webClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(bytes)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return filePath;

        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al subir archivo a Supabase. Status: " + e.getStatusCode(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error al subir archivo: " + e.getMessage(), e);
        }
    }

    // Construye la URL pública a partir del path relativo guardado en la BD.
    public String getPublicUrl(String filePath) {
        if (filePath == null || filePath.isBlank()) return null;
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + filePath;
    }

    // Elimina un archivo usando el path relativo guardado en la BD.
    public void deleteFile(String filePath) {
        try {
            webClient.delete()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + filePath)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.warn("No se pudo eliminar el archivo '{}' de Supabase. Status: {}", filePath, e.getStatusCode());
        }
    }

    private String buildUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null) {
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex > 0) extension = originalFileName.substring(dotIndex);
        }
        return UUID.randomUUID() + extension;
    }
}
