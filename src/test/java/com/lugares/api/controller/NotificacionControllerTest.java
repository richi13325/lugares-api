package com.lugares.api.controller;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.lugares.api.dto.request.NotificacionRequest;
import com.lugares.api.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest extends BaseControllerTest {

    @MockitoBean
    private NotificacionService notificacionService;

    // ================================================================== //
    //  POST /api/notificaciones/cliente/{clienteId}                       //
    // ================================================================== //

    @Test
    void enviarACliente_validRequest_returnsOkWithNoData() throws Exception {
        // given
        NotificacionRequest request = new NotificacionRequest();
        request.setTitulo("Promo especial");
        request.setMensaje("Descuento del 20% en tu proximo pedido");

        // when & then
        mockMvc.perform(post("/api/notificaciones/cliente/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("Operacion exitosa"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(notificacionService).enviarACliente(1L, "Promo especial", "Descuento del 20% en tu proximo pedido");
    }

    @Test
    void enviarACliente_firebaseError_returnsBadGateway() throws Exception {
        // given — FirebaseMessagingException has no public no-arg constructor, must mock()
        doThrow(mock(FirebaseMessagingException.class))
                .when(notificacionService).enviarACliente(anyLong(), anyString(), anyString());

        NotificacionRequest request = new NotificacionRequest();
        request.setTitulo("Test");
        request.setMensaje("Test mensaje");

        // when & then
        mockMvc.perform(post("/api/notificaciones/cliente/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Firebase Error"))
                .andExpect(jsonPath("$.message").value("Error al enviar la notificacion push"));
    }

    @Test
    void enviarACliente_blankTitulo_returnsBadRequest() throws Exception {
        // given
        NotificacionRequest request = new NotificacionRequest();
        request.setTitulo("");
        request.setMensaje("Mensaje valido");

        // when & then
        mockMvc.perform(post("/api/notificaciones/cliente/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").value("El titulo es obligatorio"));
    }

    @Test
    void enviarACliente_blankMensaje_returnsBadRequest() throws Exception {
        // given
        NotificacionRequest request = new NotificacionRequest();
        request.setTitulo("Titulo valido");
        request.setMensaje("");

        // when & then
        mockMvc.perform(post("/api/notificaciones/cliente/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.mensaje").value("El mensaje es obligatorio"));
    }

    @Test
    void enviarACliente_mensajeTooLong_returnsBadRequest() throws Exception {
        // given — 501 chars, exceeds @Size(max=500)
        NotificacionRequest request = new NotificacionRequest();
        request.setTitulo("Titulo valido");
        request.setMensaje("M".repeat(501));

        // when & then
        mockMvc.perform(post("/api/notificaciones/cliente/1")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.mensaje")
                        .value("El mensaje no puede exceder 500 caracteres"));
    }

    @Test
    void enviarACliente_unauthenticated_returnsForbidden() throws Exception {
        // given
        NotificacionRequest request = new NotificacionRequest();
        request.setTitulo("Test");
        request.setMensaje("Test");

        // when & then — no .with(asUsuario())
        mockMvc.perform(post("/api/notificaciones/cliente/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void enviarACliente_typeMismatchOnLong_returnsBadRequest() throws Exception {
        // given — clienteId is Long, "abc" can't be parsed
        NotificacionRequest request = new NotificacionRequest();
        request.setTitulo("Test");
        request.setMensaje("Test");

        // when & then
        mockMvc.perform(post("/api/notificaciones/cliente/abc")
                        .with(asUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
