package kz.don.todoapp.repository;

import kz.don.todoapp.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RedisHash
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :id")
    Wallet getWalletInfo(UUID id);

    boolean existsByUserId(UUID id);
}
