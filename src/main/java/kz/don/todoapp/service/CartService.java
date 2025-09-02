package kz.don.todoapp.service;

import kz.don.todoapp.dto.TransactionDTO;
import kz.don.todoapp.dto.request.CartRequest;
import kz.don.todoapp.dto.response.CartResponse;
import kz.don.todoapp.entity.*;
import kz.don.todoapp.enums.UserTranscationStatus;
import kz.don.todoapp.repository.CartRepository;
import kz.don.todoapp.repository.ProductRepository;
import kz.don.todoapp.repository.UserTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;
    private final UserService userService;
    private final UserTransactionRepository userTransactionRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;

    // sorry za nested code, логика выходит не простая
    @Transactional
    public HashMap<Boolean, HashMap<String, Integer>> placeOrderCart(CartRequest cartRequest) {
        HashMap<String, Integer> requestedProducts = cartRequest.getProducts();

        // Две мапы, в одной те транзакции которые возможны, а во второй которые не возможны (например запрошенное юзером количество слишком велико)
        HashMap<String, Integer> successMap = new HashMap<>();
        HashMap<String, Integer> failMap = new HashMap<>();
        double overallSum = 0;
        List<Product> productsToUpdate = new ArrayList<>();

        // Тут проверяю возможны ли транзакции и пихаю их либо в саксес или фэйл
        for (Map.Entry<String, Integer> entry : requestedProducts.entrySet()) {
            UUID productId = UUID.fromString(entry.getKey());
            int requestedQuantity = entry.getValue();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            overallSum += requestedQuantity * product.getPrice();

            if (product.getQuantity() >= requestedQuantity) {
                successMap.put(product.getTitle(), requestedQuantity);
                productsToUpdate.add(product);
            } else {
                failMap.put(product.getTitle(), requestedQuantity);
            }
        }

        User currentUser = userService.getCurrentUser();
        if (overallSum > currentUser.getWallet().getBalance()) {
            log.info(String.valueOf(overallSum));
            throw new IllegalArgumentException("Not enough money on the balance");
        }

        // то что пойдет в ретурн
        HashMap<Boolean, HashMap<String, Integer>> result = new HashMap<>();
        result.put(false, failMap);
        result.put(true, successMap);

        // Если все транзакции возможны они сохраняются, если нет то юзеру высвечивается то что нельзя купить
        if (!failMap.isEmpty()) {
            return result;
        }

        Cart cart = Cart.builder()
                .user(userService.getCurrentUser())
                .IIN(cartRequest.getIIN())
                .address(cartRequest.getAddress())
                .number(cartRequest.getNumber())
                .patronymic(cartRequest.getPatronymic())
                .secondName(cartRequest.getSecondName())
                .firstName(cartRequest.getFirstName())
                .build();

        cart = cartRepository.save(cart);

        List<UserTransaction> userTransactions = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : successMap.entrySet()) {

            Product product = productRepository.findByTitle(entry.getKey());

            long newQuantity = product.getQuantity() - entry.getValue();
            product.setQuantity(newQuantity);

            UserTransaction userTransaction = UserTransaction.builder()
                    .product(product)
                    .user(currentUser)
                    .quantity(entry.getValue())
                    .transactionStatus(UserTranscationStatus.ORDER)
                    .cart(cart)
                    .build();

            userTransactions.add(userTransaction);
        }

        productRepository.saveAll(productsToUpdate);
        userTransactionRepository.saveAll(userTransactions);

        return result;

    }

    @Transactional
    public List<CartResponse> getAllCarts() {
        List<Cart> carts = cartRepository.findAllNotDeleted();
        List<CartResponse> responses = new ArrayList<>();

        carts.forEach(cart -> {

            List<UserTransaction> transactions = cart.getTransactions();
            List<TransactionDTO> transactionDTOS = new ArrayList<>();

            transactions.forEach(userTransaction -> transactionDTOS.add(
                    TransactionDTO.builder()
                            .sum(userTransaction.getQuantity() * userTransaction.getProduct().getQuantity())
                            .pricePerPiece(userTransaction.getProduct().getPrice())
                            .userId(String.valueOf(userTransaction.getUser().getId()))
                            .transactionType(String.valueOf(userTransaction.getTransactionStatus()))
                            .quantity(userTransaction.getQuantity())
                            .productId(String.valueOf(userTransaction.getProduct().getId()))
                            .build()
            ));

            responses.add(CartResponse.builder()
                    .transactionDTOS(transactionDTOS)
                    .userId(String.valueOf(cart.getUser().getId()))
                    .IIN(cart.getIIN())
                    .address(cart.getAddress())
                    .number(cart.getNumber())
                    .patronymic(cart.getPatronymic())
                    .secondName(cart.getSecondName())
                    .firstName(cart.getFirstName())
                    .isApproved(cart.isApproved())
                    .build());
        });

        return responses;
    }

    @Transactional
    public void approvalStatusCart(String cartId, boolean isApproved) {
        Cart cart = cartRepository.findById(UUID.fromString(cartId))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        cart.setApproved(isApproved);

        cartRepository.save(cart);
    }

    @Transactional
    public void purchaseCart(String cartId) {
        Cart cart = cartRepository.findById(UUID.fromString(cartId))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        if (!cart.isApproved()) {
            throw new IllegalArgumentException("Not approved");
        }

        List<UserTransaction> transactions = cart.getTransactions();

        transactions.forEach(transaction -> productService.purchaseOrder(transaction.getId()));

        cart.setDeleted(true);
        cartRepository.save(cart);
    }

    @Transactional
    public void deleteACart(String cartId) {
        Cart cart = cartRepository.findById(UUID.fromString(cartId))
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        cart.setDeleted(true);

        cartRepository.save(cart);
    }
}
