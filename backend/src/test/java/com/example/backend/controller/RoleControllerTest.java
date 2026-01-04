package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.RoleRequest;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RoleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    RoleService roleService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createRole_success() throws Exception {
        RoleRequest request = RoleRequest.builder()
                .name("MANAGER")
                .description("Manager role")
                .build();

        RoleResponse response = RoleResponse.builder()
                .name("MANAGER")
                .description("Manager role")
                .build();

        when(roleService.create(any()))
                .thenReturn(response);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("MANAGER"))
                .andExpect(jsonPath("$.result.description").value("Manager role"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllRoles_success() throws Exception {
        RoleResponse role1 = RoleResponse.builder()
                .name("USER")
                .description("User role")
                .build();
        RoleResponse role2 = RoleResponse.builder()
                .name("ADMIN")
                .description("Admin role")
                .build();

        when(roleService.getAll())
                .thenReturn(List.of(role1, role2));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].name").value("USER"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteRole_success() throws Exception {
        doNothing().when(roleService).delete("MANAGER");

        mockMvc.perform(delete("/roles/MANAGER"))
                .andExpect(status().isOk());
    }
}
