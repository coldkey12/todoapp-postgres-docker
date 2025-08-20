package kz.don.todoapp.mappers;

import kz.don.todoapp.entity.Product;
import kz.don.todoapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserTransactionMapperUtil {

    private final ProductRepository productRepository;

    @Named("mapUuidToProduct")
    public Product mapUuidToProduct(String productId) {
        return productRepository.findById(UUID.fromString(productId)).orElse(null);
    }
}
