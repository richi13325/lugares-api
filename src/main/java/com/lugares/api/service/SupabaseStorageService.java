package com.lugares.api.service;

import com.lugares.api.exception.StorageException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class SupabaseStorageService implements StorageService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public SupabaseStorageService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(120))))
                .build();
    }

    @Override
    @SneakyThrows
    public String uploadFile(MultipartFile file, String categoria) {
        String fileName = categoria.toLowerCase() + "/" + generateUniqueFileName(file.getOriginalFilename());
        byte[] bytes = file.getBytes();

        webClient.post()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                .header("apikey", supabaseKey)
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .bodyValue(bytes)
                .retrieve()
                .toBodilessEntity()
                .onErrorMap(WebClientException.class, e ->
                        new StorageException("Error al subir archivo a Supabase: " + e.getMessage(), e))
                .block();

        return fileName;
    }

    @Override
    public void deleteFile(String fileName) {
        webClient.delete()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                .header("apikey", supabaseKey)
                .retrieve()
                .toBodilessEntity()
                .onErrorMap(WebClientException.class, e ->
                        new StorageException("Error al eliminar archivo del bucket: " + fileName, e))
                .block();
    }

    @Override
    public String getPublicUrl(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + relativePath;
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int lastDotIndex = originalFileName != null ? originalFileName.lastIndexOf('.') : -1;
        if (lastDotIndex > 0) {
            extension = originalFileName.substring(lastDotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }
}
