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
import kz.don.todoapp.repository.TaskRepository;
import kz.don.todoapp.repository.UserRepository;
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
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, TaskRepository taskRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Get all users", description = "Returns a list of all registered users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream()
                .map(this::mapToUserResponse)
                .toList());
    }

    @Transactional
    @Operation(summary = "Update user status", description = "Enable or disable a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @Parameter(description = "UUID of the user to update") @PathVariable UUID userId,
            @Parameter(description = "Set to true to enable, false to disable") @RequestParam String enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isEnabled = switch (enabled.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Invalid value for enabled: " + enabled);
        };
        log.info("Updating user status: {} to {}", userId, isEnabled);
        user.setEnabled(isEnabled);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all tasks", description = "Returns all tasks, optionally filtered by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully")
    })
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @Parameter(description = "Optional UUID of user to filter tasks by") @RequestParam(required = false) UUID userId) {

        List<Task> tasks = userId != null
                ? taskRepository.findByUserId(userId)
                : taskRepository.findAll();

        return ResponseEntity.ok(tasks.stream()
                .map(this::mapToTaskResponse)
                .toList());
    }

    @Operation(summary = "Update user details", description = "Update user information by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/update/{userId}")
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

        return ResponseEntity.ok().build();
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .taskCount(user.getTasks() != null ? user.getTasks().size() : 0)
                .build();
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

}