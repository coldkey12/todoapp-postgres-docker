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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        if (productRepository.findByTitle(product.getTitle()) != null) {
            throw new IllegalArgumentException("Product with title '" + product.getTitle() + "' already exists.");
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
        userTransaction.setUserId(user);
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

            Optional<Product> product = productRepository.findById(userTransaction.get().getProductId().getId());

            product.get().setQuantity(product.get().getQuantity() + userTransaction.get().getQuantity());
            productRepository.save(product.get());
            return;
        }

        Optional<Product> product = productRepository.findById(userTransaction.get().getProductId().getId());

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

    @Transactional
    public void deleteProduct(UUID productId) {
        Optional<Product> product = productRepository.findById(productId);

        if (userTransactionRepository.existsByProductIdAndTransactionStatus(productId, String.valueOf(UserTranscationStatus.PAID))) {
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
        userTransactionRepository.save(userTransaction.get());
    }
}
