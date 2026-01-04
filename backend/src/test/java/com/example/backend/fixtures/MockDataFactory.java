package com.example.backend.fixtures;

import com.example.backend.dto.response.AuthenticationResponse;
import com.example.backend.dto.response.IntrospectResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory class để tạo mock responses và DTOs cho testing
 * Tách biệt với TestDataBuilder để dễ maintain
 * <p>
 * Note: Methods trong class này có thể chưa được sử dụng nhưng được giữ lại
 * để sử dụng trong tương lai khi cần tạo test data
 */
@SuppressWarnings("unused")
public class MockDataFactory {

    /**
     * Tạo AuthenticationResponse với giá trị mặc định
     */
    public static AuthenticationResponse.AuthenticationResponseBuilder defaultAuthenticationResponse() {
        return AuthenticationResponse.builder()
                .token("test-token")
                .authenticated(true);
    }

    /**
     * Tạo AuthenticationResponse với token cụ thể
     */
    public static AuthenticationResponse authenticationResponseWithToken(String token) {
        return defaultAuthenticationResponse()
                .token(token)
                .build();
    }

    /**
     * Tạo IntrospectResponse với giá trị mặc định
     */
    public static IntrospectResponse.IntrospectResponseBuilder defaultIntrospectResponse() {
        return IntrospectResponse.builder()
                .valid(true);
    }

    /**
     * Tạo UserResponse với giá trị mặc định
     */
    public static UserResponse.UserResponseBuilder defaultUserResponse() {
        return UserResponse.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .fullname("Test User");
    }

    /**
     * Tạo UserResponse từ User entity
     */
    public static UserResponse userResponseFrom(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .build();
    }

    /**
     * Tạo Set<Role> với các role names
     */
    public static Set<Role> rolesSet(String... roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(Role.builder()
                    .name(roleName)
                    .description(roleName + " role")
                    .build());
        }
        return roles;
    }

    /**
     * Tạo User với timestamp cụ thể (dùng cho testing time-based logic)
     */
    public static User userWithTimestamp(LocalDateTime timestamp) {
        return TestDataBuilder.defaultUser()
                .createdDate(timestamp)
                .build();
    }
}
