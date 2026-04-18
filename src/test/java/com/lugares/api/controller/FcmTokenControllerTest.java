package com.lugares.api.controller;

import com.lugares.api.entity.FcmToken;
import com.lugares.api.service.FcmTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FcmTokenController.class)
class FcmTokenControllerTest extends BaseControllerTest {

    @MockitoBean
    private FcmTokenService fcmTokenService;

    // ================================================================== //
    //  POST /api/fcm-tokens                                               //
    // ================================================================== //

    @Test
    void registrar_validRequest_returnsCreatedWithNoData() throws Exception {
        // given
        FcmToken entity = new FcmToken();
        when(modelMapper.map(any(), eq(FcmToken.class))).thenReturn(entity);

        // when & then
        // GOTCHA: returns 201 but uses ApiResponse.noContent(), so message = "Operacion exitosa"
        //         and $.data does NOT exist
        mockMvc.perform(post("/api/fcm-tokens")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc123token\",\"plataforma\":\"ANDROID\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.message").value("Operacion exitosa"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(fcmTokenService).registrar(any());
    }

    @Test
    void registrar_allBlankOrNull_returnsBadRequest() throws Exception {
        // given — token blank, plataforma blank
        mockMvc.perform(post("/api/fcm-tokens")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"\",\"plataforma\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.token").exists())
                .andExpect(jsonPath("$.fieldErrors.plataforma").exists());
    }

    @Test
    void registrar_blankTokenOnly_returnsBadRequest() throws Exception {
        // given — only token blank, rest valid
        mockMvc.perform(post("/api/fcm-tokens")
                        .with(asClienteWithId(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"\",\"plataforma\":\"IOS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.token").exists());
    }

    @Test
    void registrar_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/fcm-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"abc123token\",\"plataforma\":\"ANDROID\"}"))
                .andExpect(status().isForbidden());
    }
}
