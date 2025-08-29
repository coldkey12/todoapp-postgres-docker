package kz.don.todoapp.service;

import kz.don.todoapp.dto.TransactionDTO;
import kz.don.todoapp.repository.UserTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTransactionsService {

    private final UserTransactionRepository userTransactionRepository;

    public List<TransactionDTO> getAllOrders() {
        List<TransactionDTO> transactionDTOS = new ArrayList<>();
        userTransactionRepository.findAll()
                .stream().filter(userTransaction -> userTransaction.getCart() == null).toList().forEach(userTransaction -> transactionDTOS.add(TransactionDTO.builder()
                        .sum(userTransaction.getQuantity() * userTransaction.getProduct().getPrice())
                        .userId(String.valueOf(userTransaction.getUser().getId()))
                        .productId(String.valueOf(userTransaction.getProduct().getId()))
                        .transactionType(String.valueOf(userTransaction.getTransactionStatus()))
                        .quantity(userTransaction.getQuantity())
                        .pricePerPiece(userTransaction.getProduct().getPrice())
                        .build()));
        return transactionDTOS;
    }
}
