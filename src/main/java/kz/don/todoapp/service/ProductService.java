package kz.don.todoapp.service;

import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.entity.Product;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.enums.UserTranscationStatus;
import kz.don.todoapp.exceptions.ProductOutOfStock;
import kz.don.todoapp.mappers.UserTransactionMapper;
import kz.don.todoapp.repository.ProductRepository;
import kz.don.todoapp.repository.UserRepository;
import kz.don.todoapp.repository.UserTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final UserRepository userRepository;
    private final UserService userService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByTitleContainingIgnoreCase(keyword);
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
            log.info("Product with title '" + product.getTitle() + "' already exists. Renewing old one and rewriting its data.");
            return;
        }
        product.setAvailable(true);

        productRepository.save(product);
    }

    public UserTransaction placeOrder(UserTransactionRequest request) {
        User user = userService.getCurrentUser();

        Optional<Product> product = productRepository.findById(UUID.fromString(request.getProductId()));

        if(!product.get().isAvailable()) {
            throw new IllegalArgumentException("Product with ID " + request.getProductId() + " is not available.");
        }

        System.out.println(user.getId() + " and " + request.getProductId());
        UserTransaction userTransactionOld = userTransactionRepository.findByUserIdAndProductId(user.getId(), UUID.fromString(request.getProductId()));

        if (product.get().getQuantity() < request.getQuantity()) {
            throw new ProductOutOfStock(product.get().getTitle());
        }
        if (userTransactionOld != null) {
            userTransactionOld.setQuantity(userTransactionOld.getQuantity() + request.getQuantity());
            userTransactionRepository.save(userTransactionOld);

            product.get().setQuantity(product.get().getQuantity() - request.getQuantity());
            productRepository.save(product.get());
            return userTransactionOld;
        }

        UserTransaction userTransaction = userTransactionMapper.toUserTransaction(request);
        userTransaction.setUser(user);
        userTransaction.setTransactionStatus(UserTranscationStatus.ORDER);

        userTransactionRepository.save(userTransaction);

        product.get().setQuantity(product.get().getQuantity() - request.getQuantity());
        productRepository.save(product.get());

        return userTransaction;
    }

    public void cancelOrder(UUID transactionId) {
        Optional<UserTransaction> userTransaction = userTransactionRepository.findById(transactionId);

        // if user paid -> update status instead of delete row
        if (userTransaction.get().getTransactionStatus() == UserTranscationStatus.PAID) {
            userTransaction.get().setTransactionStatus(UserTranscationStatus.CANCELLED);

            Optional<Product> product = productRepository.findById(userTransaction.get().getProduct().getId());

            product.get().setQuantity(product.get().getQuantity() + userTransaction.get().getQuantity());
            productRepository.save(product.get());
            return;
        }

        Optional<Product> product = productRepository.findById(userTransaction.get().getProduct().getId());

        product.get().setQuantity(product.get().getQuantity() + userTransaction.get().getQuantity());
        productRepository.save(product.get());
        userTransactionRepository.deleteById(userTransaction.get().getId());
    }

    public void addPaymentInfo(String walletId) {
        User user = userService.getCurrentUser();
        user.setSomeThirdPartyPaymentServiceWalletId(walletId);
        userRepository.save(user);
    }

    public void purchaseOrder(UUID transactionId) {
        User user = userService.getCurrentUser();
        if (user.getSomeThirdPartyPaymentServiceWalletId() == null) {
            throw new IllegalArgumentException("User has not provided payment information.");
        }
        // Tipo storonniy API dlya payment
        log.info("User ID: " + user.getId() + ", Transaction ID: " + transactionId + ", Wallet ID: " + user.getSomeThirdPartyPaymentServiceWalletId());
        Optional<UserTransaction> userTransaction = userTransactionRepository.findById(transactionId);
        userTransaction.get().setTransactionStatus(UserTranscationStatus.PAID);
        userTransactionRepository.save(userTransaction.get());
    }

    public void updateProduct(UUID productId, Product product) {
        if(product.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity cannot be negative.");
        }
        product.setAvailable(true);
        product.setId(productId);
        productRepository.save(product);
    }

    public void deleteProductById(UUID productId) {
        System.out.println("Inside of the method");
        Optional<Product> product = productRepository.findById(productId);
        List<UserTransaction> userTransactions = userTransactionRepository.existsByProductIdAndTransactionStatus(productId, UserTranscationStatus.ORDER);
        System.out.println(userTransactions.size());

        if (!userTransactions.isEmpty()) {
            product.get().setAvailable(false);
            productRepository.save(product.get());
            return;
        }

        userTransactionRepository.deleteById(product.get().getId());
        productRepository.delete(product.get());
    }

    public void shipProduct(UUID transactionId) {
        Optional<UserTransaction> userTransaction = userTransactionRepository.findById(transactionId);
        if (userTransaction.isEmpty()) {
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " does not exist.");
        }

        userTransaction.get().setTransactionStatus(UserTranscationStatus.SHIPPED);
        userTransactionRepository.delete(userTransaction.get());
    }
}
