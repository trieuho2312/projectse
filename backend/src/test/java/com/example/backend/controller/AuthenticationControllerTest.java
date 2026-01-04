package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.AuthenticationRequest;
import com.example.backend.dto.request.IntrospectRequest;
import com.example.backend.dto.request.LogoutRequest;
import com.example.backend.dto.request.RefreshTokenRequest;
import com.example.backend.dto.response.AuthenticationResponse;
import com.example.backend.dto.response.IntrospectResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    AuthenticationService authenticationService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void authenticate_success() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("testuser")
                .password("12345678")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("test-token")
                .authenticated(true)
                .build();

        when(authenticationService.authenticate(any()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.token").value("test-token"))
                .andExpect(jsonPath("$.result.authenticated").value(true));
    }

    @Test
    void authenticate_invalidCredentials() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authenticationService.authenticate(any()))
                .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void introspect_validToken() throws Exception {
        IntrospectRequest request = IntrospectRequest.builder()
                .token("valid-token")
                .build();

        IntrospectResponse response = IntrospectResponse.builder()
                .valid(true)
                .build();

        when(authenticationService.introspect(any()))
                .thenReturn(response);

        mockMvc.perform(get("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.valid").value(true));
    }

    @Test
    void introspect_invalidToken() throws Exception {
        IntrospectRequest request = IntrospectRequest.builder()
                .token("invalid-token")
                .build();

        IntrospectResponse response = IntrospectResponse.builder()
                .valid(false)
                .build();

        when(authenticationService.introspect(any()))
                .thenReturn(response);

        mockMvc.perform(get("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.valid").value(false));
    }

    @Test
    void refreshToken_success() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .token("refresh-token")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("new-token")
                .authenticated(true)
                .build();

        when(authenticationService.refreshToken(any()))
                .thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.token").value("new-token"));
    }

    @Test
    void logout_success() throws Exception {
        LogoutRequest request = LogoutRequest.builder()
                .token("token-to-logout")
                .build();

        doNothing().when(authenticationService).logout(any());

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk());
    }
}