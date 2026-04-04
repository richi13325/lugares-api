package com.lugares.api.controller;

import com.lugares.api.dto.request.ForgotPasswordRequest;
import com.lugares.api.dto.request.ResetPasswordRequest;
import com.lugares.api.dto.request.ValidateCodeRequest;
import com.lugares.api.exception.BusinessRuleException;
import com.lugares.api.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PasswordResetController.class)
class PasswordResetControllerTest extends BaseControllerTest {

    @MockBean
    private PasswordResetService passwordResetService;

    // ================================================================== //
    //  POST /auth/password/forgot                                         //
    // ================================================================== //

    @Test
    void forgotPassword_validRequest_returnsOkWithCustomMessage() throws Exception {
        // given — service does nothing (void) by default
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setCorreoElectronico("user@test.com");
        request.setTipoUsuario("Cliente");

        // when & then
        mockMvc.perform(post("/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("Codigo enviado al correo electronico"))
                .andExpect(jsonPath("$.data").doesNotExist()); // null data, @JsonInclude(NON_NULL)
    }

    @Test
    void forgotPassword_blankFields_returnsBadRequestWithFieldErrors() throws Exception {
        // given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setCorreoElectronico("");
        request.setTipoUsuario("");

        // when & then
        mockMvc.perform(post("/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists())
                .andExpect(jsonPath("$.fieldErrors.tipoUsuario").exists());
    }

    @Test
    void forgotPassword_invalidEmailFormat_returnsBadRequest() throws Exception {
        // given
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setCorreoElectronico("not-an-email");
        request.setTipoUsuario("Cliente");

        // when & then
        mockMvc.perform(post("/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico")
                        .value("Formato de correo invalido"));
    }

    // ================================================================== //
    //  POST /auth/password/validate-code                                  //
    // ================================================================== //

    @Test
    void validateCode_validRequest_returnsOkWithToken() throws Exception {
        // given
        when(passwordResetService.validateCode("user@test.com", "123456"))
                .thenReturn("secure-token-123");

        ValidateCodeRequest request = new ValidateCodeRequest();
        request.setCorreoElectronico("user@test.com");
        request.setCodigo("123456");
        request.setTipoUsuario("Cliente");

        // when & then
        mockMvc.perform(post("/auth/password/validate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data.token").value("secure-token-123"));
    }

    @Test
    void validateCode_invalidCode_returnsUnprocessableEntity() throws Exception {
        // given
        when(passwordResetService.validateCode(anyString(), anyString()))
                .thenThrow(new BusinessRuleException("Codigo invalido o expirado"));

        ValidateCodeRequest request = new ValidateCodeRequest();
        request.setCorreoElectronico("user@test.com");
        request.setCodigo("000000");
        request.setTipoUsuario("Cliente");

        // when & then
        mockMvc.perform(post("/auth/password/validate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Codigo invalido o expirado"));
    }

    @Test
    void validateCode_blankFields_returnsBadRequest() throws Exception {
        // given
        ValidateCodeRequest request = new ValidateCodeRequest();
        request.setCorreoElectronico("");
        request.setCodigo("");
        request.setTipoUsuario("");

        // when & then
        mockMvc.perform(post("/auth/password/validate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.correoElectronico").exists())
                .andExpect(jsonPath("$.fieldErrors.codigo").exists())
                .andExpect(jsonPath("$.fieldErrors.tipoUsuario").exists());
    }

    // ================================================================== //
    //  POST /auth/password/reset                                          //
    // ================================================================== //

    @Test
    void resetPassword_validRequest_returnsOkWithCustomMessage() throws Exception {
        // given — service does nothing (void) by default
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-secure-token");
        request.setNuevaContrasenia("newpass123");

        // when & then
        mockMvc.perform(post("/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("Contrasenia actualizada correctamente"))
                .andExpect(jsonPath("$.data").doesNotExist()); // null data, @JsonInclude(NON_NULL)
    }

    @Test
    void resetPassword_expiredToken_returnsUnprocessableEntity() throws Exception {
        // given
        doThrow(new BusinessRuleException("Token expirado"))
                .when(passwordResetService).resetPassword(anyString(), anyString());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setNuevaContrasenia("newpass123");

        // when & then
        mockMvc.perform(post("/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Token expirado"));
    }

    @Test
    void resetPassword_passwordTooShort_returnsBadRequest() throws Exception {
        // given — 5 chars, below @Size(min=6)
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNuevaContrasenia("abc12");

        // when & then
        mockMvc.perform(post("/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nuevaContrasenia")
                        .value("La contrasenia debe tener al menos 6 caracteres"));
    }
}
