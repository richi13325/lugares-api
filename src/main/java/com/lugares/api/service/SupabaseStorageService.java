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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class SupabaseStorageService {

    private final WebClient webClient;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public SupabaseStorageService(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(120));
        this.webClient = webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    /**
     * Uploads a file to the configured Supabase bucket under the given category folder
     * and returns the public URL.
     */
    public String uploadFile(MultipartFile file, String categoria) throws Exception {
        Path temp = null;
        String fileName = null;
        String folderPrefix = categoria.toLowerCase() + "/";

        try {
            temp = Files.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(temp.toFile());

            String uniqueName = generateUniqueFileName(file.getOriginalFilename());
            fileName = folderPrefix + uniqueName;

            byte[] bytes = Files.readAllBytes(temp);

            webClient
                    .post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                    .header("apikey", supabaseKey)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(bytes)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;

        } catch (WebClientResponseException e) {
            throw new Exception("Error al subir el archivo. Status: " + e.getStatusCode()
                    + ", Body: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new Exception("Error al subir el archivo: " + e.getMessage(), e);
        } finally {
            if (temp != null) {
                try {
                    Files.delete(temp);
                } catch (java.io.IOException ex) {
                    log.warn("No se pudo borrar el archivo temporal: {}. Causa: {}", temp, ex.getMessage());
                }
            }
        }
    }

    /**
     * Deletes a file from the bucket by its storage path (folder/filename).
     */
    public void deleteFile(String fileName) {
        webClient
                .delete()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                .header("apikey", supabaseKey)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    /**
     * Extracts the storage path (folder/filename) from a full Supabase public URL.
     * Returns null if the URL is null, empty, or does not match the expected pattern.
     */
    public String extractFileNameFromUrlWithFolder(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        String prefix = "/storage/v1/object/public/" + this.bucket + "/";
        int startIndex = url.indexOf(prefix);
        if (startIndex != -1) {
            return url.substring(startIndex + prefix.length());
        }
        return null;
    }

    private String generateUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        if (originalFileName != null) {
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalFileName.substring(lastDotIndex);
            }
        }
        return uuid + extension;
    }
}
