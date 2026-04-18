package com.lugares.api.controller;

import com.lugares.api.dto.request.ContactoRequest;
import com.lugares.api.service.ContactoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactoController.class)
class ContactoControllerTest extends BaseControllerTest {

    @MockitoBean
    private ContactoService contactoService;

    // ================================================================== //
    //  POST /api/contacto                                                 //
    // ================================================================== //

    @Test
    void enviar_validRequestUnauthenticated_returnsOkWithNoData() throws Exception {
        // given
        ContactoRequest request = new ContactoRequest();
        request.setNombre("Juan Perez");
        request.setCorreoElectronico("juan@test.com");
        request.setAsunto("Consulta general");
        request.setMensaje("Hola, tengo una consulta sobre el servicio.");

        // when & then — public endpoint, no auth required
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("Operacion exitosa"))
                .andExpect(jsonPath("$.data").doesNotExist()); // @JsonInclude(NON_NULL) + null data

        verify(contactoService).enviarContacto(
                "Juan Perez", "juan@test.com", "Consulta general",
                "Hola, tengo una consulta sobre el servicio.");
    }

    @Test
    void enviar_validRequestAuthenticated_returnsOk() throws Exception {
        // given — authenticated user should also be able to use this public endpoint
        ContactoRequest request = new ContactoRequest();
        request.setNombre("Admin User");
        request.setCorreoElectronico("admin@test.com");
        request.setAsunto("Soporte");
        request.setMensaje("Necesito ayuda con mi cuenta.");

        // when & then
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(asUsuario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void enviar_allFieldsBlank_returnsBadRequestWithAllFieldErrors() throws Exception {
        // given
        ContactoRequest request = new ContactoRequest();
        request.setNombre("");
        request.setCorreoElectronico("");
        request.setAsunto("");
        request.setMensaje("");

        // when & then
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre").exists())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists())
                .andExpect(jsonPath("$.fieldErrors.asunto").exists())
                .andExpect(jsonPath("$.fieldErrors.mensaje").exists());
    }

    @Test
    void enviar_invalidEmailFormat_returnsBadRequest() throws Exception {
        // given
        ContactoRequest request = new ContactoRequest();
        request.setNombre("Juan Perez");
        request.setCorreoElectronico("not-an-email");
        request.setAsunto("Consulta");
        request.setMensaje("Mensaje valido.");

        // when & then
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico")
                        .value("Formato de correo invalido"));
    }

    @Test
    void enviar_nombreTooLong_returnsBadRequest() throws Exception {
        // given — nombre with 101 characters (max is 100)
        String nombreLargo = "A".repeat(101);

        ContactoRequest request = new ContactoRequest();
        request.setNombre(nombreLargo);
        request.setCorreoElectronico("juan@test.com");
        request.setAsunto("Consulta");
        request.setMensaje("Mensaje valido.");

        // when & then
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nombre")
                        .value("El nombre no puede exceder 100 caracteres"));
    }

    @Test
    void enviar_mensajeTooLong_returnsBadRequest() throws Exception {
        // given — mensaje with 1001 characters (max is 1000)
        String mensajeLargo = "M".repeat(1001);

        ContactoRequest request = new ContactoRequest();
        request.setNombre("Juan Perez");
        request.setCorreoElectronico("juan@test.com");
        request.setAsunto("Consulta");
        request.setMensaje(mensajeLargo);

        // when & then
        mockMvc.perform(post("/api/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.mensaje")
                        .value("El mensaje no puede exceder 1000 caracteres"));
    }
}
