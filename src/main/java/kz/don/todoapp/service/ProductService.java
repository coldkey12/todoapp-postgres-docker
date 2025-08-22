package kz.don.todoapp.service;

import kz.don.todoapp.dto.ProductFilter;
import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.dto.response.ProductResponse;
import kz.don.todoapp.entity.Product;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.enums.UserTranscationStatus;
import kz.don.todoapp.exceptions.ProductOutOfStock;
import kz.don.todoapp.mappers.ProductMapper;
import kz.don.todoapp.mappers.UserTransactionMapper;
import kz.don.todoapp.repository.ProductRepository;
import kz.don.todoapp.repository.UserTransactionRepository;
import kz.don.todoapp.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserTransactionRepository userTransactionRepository;
    private final ProductRepository productRepository;
    private final UserTransactionMapper userTransactionMapper;
    private final UserService userService;
    private final ProductMapper productMapper;
    private final WalletRepository walletRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<ProductResponse> getAllProductsStreamFiltered(ProductFilter productFilter) {
        return productRepository.findAll().stream().sorted((p1, p2) -> switch (productFilter.getSortBy()) {
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
    }

    public Page<ProductResponse> getAllProductsPaginated(ProductFilter productFilter) {
        Page<Product> productPage = productRepository.findAll(PageRequest.of(productFilter.getPage(), productFilter.getSize()));
        List<Product> products = productPage.getContent();
        List<ProductResponse> productResponses = products.stream().map(productMapper::toProductResponse).toList();
        return new PageImpl<>(productResponses, PageRequest.of(productFilter.getPage(), productFilter.getSize()), productPage.getTotalElements());
    }

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

        productRepository.save(product);
    }

    public UserTransaction placeOrder(UserTransactionRequest request) {
        User user = userService.getCurrentUser();

        Product product = productRepository.findById(UUID.fromString(request.getProductId())).orElseThrow(() -> new IllegalArgumentException("Product with that ID doesn't exist."));

        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Product with that ID is not available.");
        }
        UserTransaction userTransactionOld = userTransactionRepository.findByUserIdAndProductId(user.getId(), UUID.fromString(request.getProductId()));

        if (product.getQuantity() < request.getQuantity()) {
            throw new ProductOutOfStock(product.getTitle());
        }
//        if (userTransactionOld != null) {
//            userTransactionOld.setQuantity(userTransactionOld.getQuantity() + request.getQuantity());
//            userTransactionRepository.save(userTransactionOld);
//
//            product.setQuantity(product.getQuantity() - request.getQuantity());
//            productRepository.save(product);
//            return userTransactionOld;
//        }

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
    }

    public void deleteProductById(UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        List<UserTransaction> userTransactions = userTransactionRepository.existsByProductIdAndTransactionStatus(productId, UserTranscationStatus.ORDER);
        System.out.println(userTransactions.size());

        if (!userTransactions.isEmpty()) {
            if (product.isEmpty()) {
                throw new IllegalArgumentException("Product with that ID doesn't exist");
            }
            product.get().setAvailable(false);
            productRepository.save(product.get());
            return;
        }
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product with that ID doesn't exist");
        }

        userTransactionRepository.deleteById(product.get().getId());
        productRepository.delete(product.get());
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
