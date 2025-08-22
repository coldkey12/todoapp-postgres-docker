package kz.don.todoapp.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import kz.don.todoapp.dto.EnabledRequest;
import kz.don.todoapp.dto.response.UserResponse;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.enums.RoleEnum;
import kz.don.todoapp.mappers.UserMapper;
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
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toListUserResponse(users);
    }

    @Transactional
    @PutMapping("/{userId}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody EnabledRequest enabled
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        log.info("Updating user status: {} to {}", userId, enabled);
        user.setEnabled(enabled.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/update")
    public ResponseEntity<String> updateUserDetails(
            @PathVariable UUID userId,
            @RequestParam String username,
            @RequestParam String fullName,
            @RequestParam String password,
            @RequestParam String role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setUsername(username);
        user.setRole(RoleEnum.valueOf(role.toUpperCase()));
        user.setFullName(fullName);
        user.setEnabled(true);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        return ResponseEntity.ok(String.valueOf(userRepository.save(user)));
    }
}
