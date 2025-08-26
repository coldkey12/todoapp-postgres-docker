package kz.don.todoapp.service;

import kz.don.todoapp.dto.ProductFilter;
import kz.don.todoapp.dto.TransactionDTO;
import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.dto.response.ProductResponse;
import kz.don.todoapp.entity.Product;
import kz.don.todoapp.entity.RefreshToken;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.enums.UserTranscationStatus;
import kz.don.todoapp.exceptions.ProductOutOfStock;
import kz.don.todoapp.mappers.ProductMapper;
import kz.don.todoapp.mappers.UserTransactionMapper;
import kz.don.todoapp.repository.ProductRepository;
import kz.don.todoapp.repository.RefreshTokenRepository;
import kz.don.todoapp.repository.UserTransactionRepository;
import kz.don.todoapp.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RestTemplate restTemplate;
    private final UserTransactionRepository userTransactionRepository;
    private final ProductRepository productRepository;
    private final UserTransactionMapper userTransactionMapper;
    private final UserService userService;
    private final ProductMapper productMapper;
    private final WalletRepository walletRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_RESPONSE_CACHE = "PRODUCT_RESPONSE_CACHE";
    private static final String PRODUCT_PAGES_CACHE = "PRODUCT_PAGES_CACHE";
    private static final String PRODUCT_CACHE = "PRODUCT_CACHE";

    private String getProductFilterKey(String cacheName, ProductFilter filter) {
        return cacheName + "::" + filter.toString();
    }

    private <T> T getFromCache(String key, Class<T> type) {
        Object cached = redisTemplate.opsForValue().get(key);
        return type.isInstance(cached) ? type.cast(cached) : null;
    }

    private void setInCache(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    private void evictCacheByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<ProductResponse> getAllProductsStreamFiltered(ProductFilter productFilter) {
        String cacheKey = getProductFilterKey(PRODUCT_RESPONSE_CACHE, productFilter);
        List<ProductResponse> cached = getFromCache(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }
        List<ProductResponse> result = productRepository.findAll().stream().sorted((p1, p2) -> switch (productFilter.getSortBy()) {
                    case "price" ->
                            "desc".equalsIgnoreCase(productFilter.getDirection()) ? p2.getPrice().compareTo(p1.getPrice()) : p1.getPrice().compareTo(p2.getPrice());
                    case "quantity" ->
                            "desc".equalsIgnoreCase(productFilter.getDirection()) ? p2.getQuantity().compareTo(p1.getQuantity()) : p1.getQuantity().compareTo(p2.getQuantity());
                    default -> 0;
                })
                .limit(productFilter.getAmount())
                .map(productMapper::toProductResponse)
                .filter(product -> product.getQuantity() > 500)
                .toList();

        setInCache(cacheKey, result);
        return result;
    }

    public Page<ProductResponse> getAllProductsPaginated(ProductFilter productFilter) {
        String cacheKey = getProductFilterKey(PRODUCT_PAGES_CACHE, productFilter);
        Page<ProductResponse> cached = getFromCache(cacheKey, Page.class);
        if (cached != null) {
            return cached;
        }

        Page<Product> productPage = productRepository.findAll(PageRequest.of(productFilter.getPage(), productFilter.getSize()));
        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .toList();

        Page<ProductResponse> result = new PageImpl<>(
                productResponses,
                PageRequest.of(productFilter.getPage(), productFilter.getSize()),
                productPage.getTotalElements()
        );

        setInCache(cacheKey, result);
        return result;
    }

    @Transactional
    public void createProduct(Product product) {
        Product productCheck = productRepository.findByTitle(product.getTitle());

        if (productCheck != null) {
            productCheck.setAvailable(true);
            productCheck.setQuantity(product.getQuantity());
            productCheck.setDescription(product.getDescription());
            productCheck.setPrice(product.getPrice());
            productCheck.setTitle(product.getTitle());
            productRepository.save(productCheck);
            log.info("Product with title {} already exists. Renewing old one and rewriting its data.", product.getTitle());
            return;
        }
        product.setAvailable(true);
        product = productRepository.save(product);

        HttpHeaders headers = new HttpHeaders();

        RefreshToken refreshToken = refreshTokenRepository.findByUser(userService.getCurrentUser())
                .orElseThrow(() -> new IllegalArgumentException("Token with that user doesn't exist"));

        headers.setBearerAuth(refreshToken.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        double sum = product.getPrice() * product.getQuantity();

        TransactionDTO dto = TransactionDTO.builder()
                .sum(sum)
                .pricePerPiece(product.getPrice())
                .transactionType("INVENTORY")
                .userId(String.valueOf(userService.getCurrentUser().getId()))
                .quantity(Math.toIntExact(product.getQuantity()))
                .productId(String.valueOf(product.getId()))
                .build();

        HttpEntity<TransactionDTO> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8081/accounting",
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to send transaction: " + response.getStatusCode());
        }

        evictCacheByPattern(PRODUCT_RESPONSE_CACHE + "*");
        evictCacheByPattern(PRODUCT_PAGES_CACHE + "*");
    }

    @Transactional
    public UserTransaction placeOrder(UserTransactionRequest request) {
        User user = userService.getCurrentUser();

        Product product = productRepository.findById(UUID.fromString(request.getProductId())).orElseThrow(() -> new IllegalArgumentException("Product with that ID doesn't exist."));

        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Product with that ID is not available.");
        }

        if (product.getQuantity() < request.getQuantity()) {
            throw new ProductOutOfStock(product.getTitle());
        }

        UserTransaction userTransaction = userTransactionMapper.toUserTransaction(request);
        userTransaction.setUser(user);
        userTransaction.setTransactionStatus(UserTranscationStatus.ORDER);

        userTransactionRepository.save(userTransaction);

        product.setQuantity(product.getQuantity() - request.getQuantity());
        productRepository.save(product);

        return userTransaction;
    }

    @Transactional
    public void cancelOrder(UUID transactionId) {
        UserTransaction userTransaction = userTransactionRepository.findById(transactionId).orElseThrow(() -> new IllegalArgumentException("User transaction with that ID doesn't exist."));

        // if user paid -> update status instead of delete row
        if (userTransaction.getTransactionStatus().equals(UserTranscationStatus.PAID)) {
            userTransaction.setTransactionStatus(UserTranscationStatus.CANCELLED);

            Product product = productRepository.findById(userTransaction.getProduct().getId()).orElseThrow(() -> new IllegalArgumentException("Product with that ID doesn't exist."));

            product.setQuantity(product.getQuantity() + userTransaction.getQuantity());
            productRepository.save(product);
            return;
        }

        Product product = productRepository.findById(userTransaction.getProduct().getId()).orElseThrow(() -> new IllegalArgumentException("Product with that ID doesn't exist."));

        product.setQuantity(product.getQuantity() + userTransaction.getQuantity());
        productRepository.save(product);
        userTransactionRepository.deleteById(userTransaction.getId());
    }

    @Transactional
    public void purchaseOrder(UUID transactionId) {
        User user = userService.getCurrentUser();
        if (user.getWallet() == null) {
            throw new IllegalArgumentException("User has not provided payment information.");
        }
        log.info("Processing payment for transaction ID: {} using wallet ID: {}", transactionId, user.getWallet().getId());

        UserTransaction userTransaction = userTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction with that ID doesn't exist"));

        if (user.getWallet().getBalance() < userTransaction.getProduct().getPrice() * userTransaction.getQuantity()) {
            throw new IllegalArgumentException("Insufficient balance in wallet.");
        }

        user.getWallet().setBalance(user.getWallet().getBalance() - userTransaction.getProduct().getPrice() * userTransaction.getQuantity());
        walletRepository.save(user.getWallet());

        Product product = userTransaction.getProduct();

        HttpHeaders headers = new HttpHeaders();

        RefreshToken refreshToken = refreshTokenRepository.findByUser(userService.getCurrentUser())
                .orElseThrow(() -> new IllegalArgumentException("Token with that user doesn't exist"));

        headers.setBearerAuth(refreshToken.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        double sum = product.getPrice() * userTransaction.getQuantity();

        TransactionDTO dto = TransactionDTO.builder()
                .sum(sum)
                .pricePerPiece(product.getPrice())
                .transactionType("PURCHASE")
                .userId(String.valueOf(userService.getCurrentUser().getId()))
                .quantity(Math.toIntExact(userTransaction.getQuantity()))
                .productId(String.valueOf(product.getId()))
                .build();

        HttpEntity<TransactionDTO> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8081/accounting",
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to send transaction: " + response.getStatusCode());
        }

        userTransaction.setTransactionStatus(UserTranscationStatus.PAID);
        userTransactionRepository.save(userTransaction);
    }

    public void updateProduct(UUID productId, Product product) {
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative.");
        }
        product.setAvailable(true);
        product.setId(productId);
        productRepository.save(product);

        String productKey = PRODUCT_CACHE + "::" + productId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(productKey))) {
            setInCache(productKey, productMapper.toProductResponse(product));
        }

        evictCacheByPattern(PRODUCT_RESPONSE_CACHE + "*");
        evictCacheByPattern(PRODUCT_PAGES_CACHE + "*");
    }

    public void deleteProductById(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Token with that user doesn't exist"));
        List<UserTransaction> userTransactions = userTransactionRepository.existsByProductIdAndTransactionStatus(productId, UserTranscationStatus.ORDER);
        System.out.println(userTransactions.size());

        if (!userTransactions.isEmpty()) {
            if (product != null) {
                throw new IllegalArgumentException("Product with that ID doesn't exist");
            }
            return;
        }
        if (product == null) {
            throw new IllegalArgumentException("Product with that ID doesn't exist");
        }

        HttpHeaders headers = new HttpHeaders();

        RefreshToken refreshToken = refreshTokenRepository.findByUser(userService.getCurrentUser())
                .orElseThrow(() -> new IllegalArgumentException("Token with that user doesn't exist"));

        headers.setBearerAuth(refreshToken.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        double sum = product.getPrice() * product.getQuantity();

        TransactionDTO dto = TransactionDTO.builder()
                .sum(sum)
                .pricePerPiece(product.getPrice())
                .transactionType("REMOVED")
                .userId(String.valueOf(userService.getCurrentUser().getId()))
                .quantity(Math.toIntExact(product.getQuantity()))
                .productId(String.valueOf(product.getId()))
                .build();

        HttpEntity<TransactionDTO> entity = new HttpEntity<>(dto, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8081/accounting",
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to send transaction: " + response.getStatusCode());
        }

        userTransactionRepository.deleteById(product.getId());
        productRepository.delete(product);

        redisTemplate.delete(PRODUCT_CACHE + "::" + productId);
        evictCacheByPattern(PRODUCT_RESPONSE_CACHE + "*");
        evictCacheByPattern(PRODUCT_PAGES_CACHE + "*");
    }

    public void shipProduct(UUID transactionId) {
        Optional<UserTransaction> userTransaction = userTransactionRepository.findById(transactionId);
        if (userTransaction.isEmpty()) {
            throw new IllegalArgumentException("Transaction with that ID doesn't exist");
        }

        userTransaction.get().setTransactionStatus(UserTranscationStatus.SHIPPED);
        userTransactionRepository.delete(userTransaction.get());
    }
}
