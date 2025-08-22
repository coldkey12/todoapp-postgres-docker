package kz.don.todoapp.controller.user;

import kz.don.todoapp.dto.ProductFilter;
import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.dto.response.ProductResponse;
import kz.don.todoapp.entity.Product;
import kz.don.todoapp.enums.UserTranscationStatus;
import kz.don.todoapp.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/marketplace")
@PreAuthorize("hasRole('USER')")
@Slf4j
public class MarketplaceController {

    private final ProductService productService;

    public MarketplaceController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/pagination")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(@RequestBody ProductFilter productFilter) {
        return ResponseEntity.ok(productService.getAllProductsPaginated(productFilter));
    }

    @GetMapping("/products/stream")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestBody ProductFilter productFilter) {
        return ResponseEntity.ok(productService.getAllProductsStreamFiltered(productFilter));
    }

    @PostMapping("/place-order")
    public ResponseEntity<UserTranscationStatus> placeOrder(@RequestBody UserTransactionRequest request) {
        return ResponseEntity.ok(productService.placeOrder(request).getTransactionStatus());
    }

    @DeleteMapping("/cancel-order/{transactionId}")
    public ResponseEntity<String> cancelOrder(@PathVariable UUID transactionId) {
        productService.cancelOrder(transactionId);
        return ResponseEntity.ok("Order cancelled successfully");
    }

    @PostMapping("/purchase/{transactionId}")
    public ResponseEntity<String> purchaseOrder(@PathVariable UUID transactionId) {
        productService.purchaseOrder(transactionId);
        return ResponseEntity.ok("Order purchased successfully");
    }

    @PutMapping("/add-payment-info")
    public ResponseEntity<String> addPaymentInfo(@RequestParam String walletId) {
        productService.addPaymentInfo(walletId);
        return ResponseEntity.ok("Payment information added successfully");
    }
}
