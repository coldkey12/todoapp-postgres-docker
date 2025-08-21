package kz.don.todoapp.controller.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import kz.don.todoapp.dto.EnabledRequest;
import kz.don.todoapp.dto.request.ProductRequest;
import kz.don.todoapp.dto.response.UserResponse;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.enums.RoleEnum;
import kz.don.todoapp.mappers.ProductMapper;
import kz.don.todoapp.mappers.UserMapper;
import kz.don.todoapp.repository.UserRepository;
import kz.don.todoapp.service.ProductService;
import kz.don.todoapp.service.UserTransactionsService;
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
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProductMapper productMapper;
    private final ProductService productService;
    private final UserTransactionsService userTransactionsService;

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toListUserResponse(users);
    }

    @Transactional
    @PutMapping("/users/{userId}/status")
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

    @PutMapping("/users/{userId}/update")
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

    @PostMapping("/add-product")
    public ResponseEntity<String> addProduct(@RequestBody ProductRequest productRequest) {
        log.info("Adding product: {} with price: {}", productRequest.getTitle(), productRequest.getPrice());
        productService.createProduct(productMapper.toProduct(productRequest));
        return ResponseEntity.ok("Product added successfully");
    }

    @PutMapping("/update-product/{productId}")
    public ResponseEntity<String> updateProduct(
            @PathVariable UUID productId,
            @RequestBody ProductRequest productRequest) {
        log.info("Updating product with ID: {}", productId);
        productService.updateProduct(productId, productMapper.toProduct(productRequest));
        return ResponseEntity.ok("Product updated successfully");
    }

    @DeleteMapping("/delete-product/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productId) {
        log.info("Deleting product with ID: {}", productId);

        productService.deleteProductById(UUID.fromString(productId));
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PutMapping("/ship-product/{transactionId}")
    public ResponseEntity<String> shipProduct(@PathVariable UUID transactionId) {
        log.info("Shipping product with transaction ID: {}", transactionId);
        productService.shipProduct(transactionId);
        return ResponseEntity.ok("Product shipped successfully");
    }

    @GetMapping("/orders")
    public List<UserTransaction> getAllOrders() {
        log.info("Fetching all orders");
        return userTransactionsService.getAllOrders();
    }
}