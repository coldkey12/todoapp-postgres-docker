package kz.don.todoapp.controller;

import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.entity.Product;
import kz.don.todoapp.enums.UserTranscationStatus;
import kz.don.todoapp.service.ProductService;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/products/filters")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam String sortBy, // price / quantity
            @RequestParam String sortDirection, // asc / desc
            @RequestParam int n) {
        if (n <= 0) {
            n = 5;
        }
        return ResponseEntity.ok(productService.getAllProductsFiltered(sortBy, sortDirection, n));
    }

    @GetMapping("/products/{keyword}")
    public ResponseEntity<List<Product>> searchProducts(
            @PathVariable String keyword, // title / description
            @RequestParam String sortBy, // price / quantity
            @RequestParam String sortDirection, // asc / desc
            @RequestParam int n
    ) {
        if (n <= 0) {
            n = 5;
        }
        return ResponseEntity.ok(productService.searchProducts(keyword, sortBy, sortDirection, n));
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
