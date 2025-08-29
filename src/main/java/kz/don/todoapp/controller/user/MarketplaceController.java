package kz.don.todoapp.controller.user;

import jakarta.validation.Valid;
import kz.don.todoapp.dto.ProductFilter;
import kz.don.todoapp.dto.request.CartRequest;
import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.dto.response.ProductResponse;
import kz.don.todoapp.entity.Product;
import kz.don.todoapp.enums.UserTranscationStatus;
import kz.don.todoapp.service.CartService;
import kz.don.todoapp.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/marketplace")
@PreAuthorize("hasRole('USER')")
@Slf4j
public class MarketplaceController {

    private final ProductService productService;
    private final CartService cartService;

    public MarketplaceController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
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

    // если чето не так допустим ИИН не ввел правильно он выведет, в exceptions folder добавил GlobalExceptionHandler
    @PostMapping("/cart")
    public ResponseEntity<HashMap<Boolean, HashMap<String, Integer>>> placeOrderCart(@Valid @RequestBody CartRequest cartRequest) {
        HashMap<Boolean, HashMap<String, Integer>> result = cartService.placeOrderCart(cartRequest);
        if (!result.get(false).isEmpty()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cart/purchase/{cartId}")
    public ResponseEntity<String> purchaseCart(@PathVariable String cartId) {
        cartService.purchaseCart(cartId);
        return ResponseEntity.ok("Cart purchased successfully");
    }
}
