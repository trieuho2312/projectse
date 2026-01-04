package com.example.backend.fixtures;

import com.example.backend.constant.PredefinedRole;
import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.AuthenticationRequest;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Builder class để tạo test data một cách dễ dàng và nhất quán
 * Sử dụng Builder pattern để tạo các đối tượng test data
 * <p>
 * Note: Methods trong class này có thể chưa được sử dụng nhưng được giữ lại
 * để sử dụng trong tương lai khi cần tạo test data
 */
@SuppressWarnings("unused")
public class TestDataBuilder {

    /**
     * Tạo User entity với giá trị mặc định
     */
    public static User.UserBuilder defaultUser() {
        return User.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .fullname("Test User")
                .password("encoded-password")
                .createdDate(LocalDateTime.now());
    }

    /**
     * Tạo User entity với username cụ thể
     */
    public static User.UserBuilder userWithUsername(String username) {
        return defaultUser().username(username)
                .email(username + "@sis.hust.edu.vn");
    }

    /**
     * Tạo User entity với email cụ thể
     */
    public static User.UserBuilder userWithEmail(String email) {
        return defaultUser().email(email);
    }

    /**
     * Tạo User entity với roles
     */
    public static User userWithRoles(String... roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(Role.builder()
                    .name(roleName)
                    .description(roleName + " role")
                    .build());
        }
        return defaultUser().roles(roles).build();
    }

    /**
     * Tạo Role entity với giá trị mặc định
     */
    public static Role.RoleBuilder defaultRole() {
        return Role.builder()
                .name(PredefinedRole.USER_ROLE)
                .description("User role");
    }

    /**
     * Tạo Role entity với tên cụ thể
     */
    public static Role roleWithName(String roleName) {
        return defaultRole()
                .name(roleName)
                .description(roleName + " role")
                .build();
    }

    /**
     * Tạo UserCreationRequest với giá trị mặc định
     */
    public static UserCreationRequest.UserCreationRequestBuilder defaultUserCreationRequest() {
        return UserCreationRequest.builder()
                .username("newuser")
                .password("12345678")
                .fullname("New User")
                .email("newuser@sis.hust.edu.vn");
    }

    /**
     * Tạo UserUpdateRequest với giá trị mặc định
     */
    public static UserUpdateRequest.UserUpdateRequestBuilder defaultUserUpdateRequest() {
        return UserUpdateRequest.builder()
                .fullname("Updated Name")
                .email("updated@sis.hust.edu.vn");
    }

    /**
     * Tạo AuthenticationRequest với giá trị mặc định
     */
    public static AuthenticationRequest.AuthenticationRequestBuilder defaultAuthenticationRequest() {
        return AuthenticationRequest.builder()
                .username("testuser")
                .password("12345678");
    }

    /**
     * Tạo AddressDTO với giá trị mặc định
     */
    public static AddressDTO.AddressDTOBuilder defaultAddressDTO() {
        return AddressDTO.builder()
                .name("Test Address")
                .phone("0123456789")
                .addressDetail("123 Test Street")
                .wardCode("001");
    }

    /**
     * Tạo User entity đầy đủ với tất cả fields (dùng cho integration tests)
     */
    public static User completeUser() {
        Role userRole = roleWithName(PredefinedRole.USER_ROLE);
        return defaultUser()
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
    }

    /**
     * Tạo User entity với role ADMIN
     */
    public static User adminUser() {
        return userWithRoles(PredefinedRole.ADMIN_ROLE);
    }

    /**
     * Tạo User entity với role USER
     */
    public static User regularUser() {
        return userWithRoles(PredefinedRole.USER_ROLE);
    }
}
