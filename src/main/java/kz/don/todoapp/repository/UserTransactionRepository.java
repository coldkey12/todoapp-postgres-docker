package kz.don.todoapp.repository;

import kz.don.todoapp.entity.UserTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserTransactionRepository extends JpaRepository<UserTransaction, UUID> {

    @Query("SELECT ut FROM UserTransaction ut WHERE ut.userId.id = :userId AND ut.productId.id = :productId")
    UserTransaction findByUserIdAndProductId(UUID userId, UUID productId);

    void deleteById(UUID userTransactionId);

    @Query("SELECT ut FROM UserTransaction ut WHERE ut.productId = :productId AND ut.transactionStatus = :transactionStatus")
    boolean existsByProductIdAndTransactionStatus(UUID productId, String transactionStatus);

//    List<Product> findByProductId(Product product);
}
