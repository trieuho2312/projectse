package com.example.backend.mapper;

import com.example.backend.dto.request.RoleRequest;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RoleMapperTest {

    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        roleMapper = new RoleMapperImpl();
    }

    @Test
    void toRole_shouldMapCorrectly() {
        // Arrange
        RoleRequest request = RoleRequest.builder()
                .name("ADMIN")
                .description("Administrator role")
                .permissions(new HashSet<>(Set.of("READ", "WRITE")))
                .build();

        // Act
        Role role = roleMapper.toRole(request);

        // Assert
        assertNotNull(role);
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
    }

    @Test
    void toRole_nullRequest_returnsNull() {
        // Act
        Role role = roleMapper.toRole(null);

        // Assert
        assertNull(role);
    }

    @Test
    void toRoleResponse_shouldMapCorrectly() {
        // Arrange
        Role role = Role.builder()
                .name("ADMIN")
                .description("Administrator role")
                .build();

        // Act
        RoleResponse response = roleMapper.toRoleResponse(role);

        // Assert
        assertNotNull(response);
        assertEquals("ADMIN", response.getName());
        assertEquals("Administrator role", response.getDescription());
    }

    @Test
    void toRoleResponse_nullRole_returnsNull() {
        // Act
        RoleResponse response = roleMapper.toRoleResponse(null);

        // Assert
        assertNull(response);
    }
}
