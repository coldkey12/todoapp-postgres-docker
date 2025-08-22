package kz.don.todoapp.controller.admin;

import kz.don.todoapp.dto.request.ProductRequest;
import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.mappers.ProductMapper;
import kz.don.todoapp.service.ProductService;
import kz.don.todoapp.service.UserTransactionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/marketplace")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminMarketplaceController {

    private final ProductMapper productMapper;
    private final ProductService productService;
    private final UserTransactionsService userTransactionsService;

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