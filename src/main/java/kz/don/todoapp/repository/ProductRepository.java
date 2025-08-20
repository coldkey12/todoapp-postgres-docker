package kz.don.todoapp.repository;

import kz.don.todoapp.entity.Product;
import kz.don.todoapp.entity.UserTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("SELECT p FROM Product p WHERE p.title LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> findByTitleContainingIgnoreCase(String keyword);

    Product findByTitle(String title);

    Optional<UserTransaction> findById(Product productId);
}
