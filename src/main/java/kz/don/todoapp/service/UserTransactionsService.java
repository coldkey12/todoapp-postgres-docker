package kz.don.todoapp.service;

import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.repository.UserTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserTransactionsService {

    private final UserTransactionRepository userTransactionRepository;

    public List<UserTransaction> getAllOrders() {
        return userTransactionRepository.findAll();
    }
}
