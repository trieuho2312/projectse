package com.example.backend.configuration;

import com.example.backend.constant.PredefinedRole;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationInitConfigTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private ApplicationInitConfig applicationInitConfig;

    private ApplicationRunner applicationRunner;

    @BeforeEach
    void setUp() {
        applicationRunner = applicationInitConfig.applicationRunner(userRepository, roleRepository);
    }

    @Test
    void applicationRunner_shouldCreateUserRoleIfMissing() throws Exception {
        // Arrange
        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.empty());
        when(roleRepository.findById(PredefinedRole.ADMIN_ROLE)).thenReturn(Optional.of(
                Role.builder().name(PredefinedRole.ADMIN_ROLE).description("Admin role").build()));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().build()));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

        // Act
        applicationRunner.run(null);

        // Assert
        verify(roleRepository, times(1)).save(roleCaptor.capture());
        Role savedRole = roleCaptor.getValue();
        assertEquals(PredefinedRole.USER_ROLE, savedRole.getName());
        assertEquals("User role", savedRole.getDescription());
    }

    @Test
    void applicationRunner_shouldNotCreateUserRoleIfExists() throws Exception {
        // Arrange
        Role existingUserRole = Role.builder()
                .name(PredefinedRole.USER_ROLE)
                .description("User role")
                .build();

        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(existingUserRole));
        when(roleRepository.findById(PredefinedRole.ADMIN_ROLE)).thenReturn(Optional.of(
                Role.builder().name(PredefinedRole.ADMIN_ROLE).description("Admin role").build()));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().build()));

        // Act
        applicationRunner.run(null);

        // Assert
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void applicationRunner_shouldCreateAdminRoleIfMissing() throws Exception {
        // Arrange
        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(
                Role.builder().name(PredefinedRole.USER_ROLE).description("User role").build()));
        when(roleRepository.findById(PredefinedRole.ADMIN_ROLE)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(User.builder().build()));

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

        // Act
        applicationRunner.run(null);

        // Assert
        verify(roleRepository, times(1)).save(roleCaptor.capture());
        Role savedRole = roleCaptor.getValue();
        assertEquals(PredefinedRole.ADMIN_ROLE, savedRole.getName());
        assertEquals("Admin role", savedRole.getDescription());
    }

    @Test
    void applicationRunner_shouldCreateAdminUserIfMissing() throws Exception {
        // Arrange
        Role userRole = Role.builder().name(PredefinedRole.USER_ROLE).description("User role").build();
        Role adminRole = Role.builder().name(PredefinedRole.ADMIN_ROLE).description("Admin role").build();

        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(userRole));
        when(roleRepository.findById(PredefinedRole.ADMIN_ROLE)).thenReturn(Optional.of(adminRole));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin")).thenReturn("encoded-admin-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        applicationRunner.run(null);

        // Assert
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("admin", savedUser.getUsername());
        assertEquals("encoded-admin-password", savedUser.getPassword());
        assertNotNull(savedUser.getRoles());
        assertTrue(savedUser.getRoles().contains(adminRole));
    }

    @Test
    void applicationRunner_shouldNotCreateAdminUserIfExists() throws Exception {
        // Arrange
        Role userRole = Role.builder().name(PredefinedRole.USER_ROLE).description("User role").build();
        Role adminRole = Role.builder().name(PredefinedRole.ADMIN_ROLE).description("Admin role").build();
        User existingAdmin = User.builder()
                .username("admin")
                .build();

        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(userRole));
        when(roleRepository.findById(PredefinedRole.ADMIN_ROLE)).thenReturn(Optional.of(adminRole));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        // Act
        applicationRunner.run(null);

        // Assert
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void applicationRunner_shouldEncodeAdminPassword() throws Exception {
        // Arrange
        Role userRole = Role.builder().name(PredefinedRole.USER_ROLE).description("User role").build();
        Role adminRole = Role.builder().name(PredefinedRole.ADMIN_ROLE).description("Admin role").build();

        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(userRole));
        when(roleRepository.findById(PredefinedRole.ADMIN_ROLE)).thenReturn(Optional.of(adminRole));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin")).thenReturn("encoded-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        applicationRunner.run(null);

        // Assert
        verify(passwordEncoder, times(1)).encode("admin");
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
    }

    @Test
    void applicationRunner_shouldAssignAdminRoleToAdminUser() throws Exception {
        // Arrange
        Role userRole = Role.builder().name(PredefinedRole.USER_ROLE).description("User role").build();
        Role adminRole = Role.builder().name(PredefinedRole.ADMIN_ROLE).description("Admin role").build();

        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(userRole));
        when(roleRepository.findById(PredefinedRole.ADMIN_ROLE)).thenReturn(Optional.of(adminRole));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin")).thenReturn("encoded-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // Act
        applicationRunner.run(null);

        // Assert
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        Set<Role> roles = savedUser.getRoles();
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(adminRole));
    }
}
