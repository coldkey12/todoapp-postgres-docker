package kz.don.todoapp.mappers;

import kz.don.todoapp.dto.request.ProductRequest;
import kz.don.todoapp.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductRequest productRequest);
}
