package kz.don.todoapp.controller;

import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.entity.Product;
import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/marketplace")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public class MarketplaceController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{keyword}")
    public ResponseEntity<List<Product>> searchProducts(@PathVariable String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @PostMapping("/place-order")
    public UserTransaction placeOrder(@RequestBody UserTransactionRequest request) {
        return productService.placeOrder(request);
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
    public ResponseEntity<String> addPaymentInfo(@RequestBody String walletId) {
        productService.addPaymentInfo(walletId);
        return ResponseEntity.ok("Payment information added successfully");
    }

}
