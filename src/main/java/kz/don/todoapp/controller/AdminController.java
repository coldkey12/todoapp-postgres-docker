package kz.don.todoapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import kz.don.todoapp.dto.response.TaskResponse;
import kz.don.todoapp.dto.response.UserResponse;
import kz.don.todoapp.entity.Task;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.enums.RoleEnum;
import kz.don.todoapp.mappers.TaskMapper;
import kz.don.todoapp.mappers.UserMapper;
import kz.don.todoapp.repository.TaskRepository;
import kz.don.todoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Controller", description = "Endpoints for admin operations")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    })
    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toListUserResponse(users);
    }

    @Transactional
    @Operation(summary = "Update user status", description = "Enable or disable a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID userId,
            @Parameter(description = "Set to true to enable, false to disable") @RequestParam boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        log.info("Updating user status: {} to {}", userId, enabled);
        user.setEnabled(enabled);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update user details", description = "Update user information by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User details updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userId}/update")
    public ResponseEntity<Void> updateUserDetails(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID userId,
            @Parameter(description = "New username for the user") @RequestParam String username,
            @Parameter(description = "New full name for the user") @RequestParam String fullName,
            @Parameter(description = "New password for the user") @RequestParam String password,
            @Parameter(description = "New role for the user") @RequestParam String role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setUsername(username);
        user.setRole(RoleEnum.valueOf(role.toUpperCase()));
        user.setFullName(fullName);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }
}