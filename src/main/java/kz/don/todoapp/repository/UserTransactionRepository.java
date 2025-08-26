package kz.don.todoapp.repository;

import kz.don.todoapp.entity.UserTransaction;
import kz.don.todoapp.enums.UserTranscationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RedisHash
public interface UserTransactionRepository extends JpaRepository<UserTransaction, UUID> {

    void deleteById(UUID userTransactionId);

    @Query("SELECT ut FROM UserTransaction ut WHERE ut.product.id = :productId AND ut.transactionStatus = :transactionStatus")
    List<UserTransaction> existsByProductIdAndTransactionStatus(UUID productId, UserTranscationStatus transactionStatus);

}
