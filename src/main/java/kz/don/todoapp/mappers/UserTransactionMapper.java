package kz.don.todoapp.mappers;

import kz.don.todoapp.dto.request.UserTransactionRequest;
import kz.don.todoapp.entity.UserTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserTransactionMapperUtil.class)
public interface UserTransactionMapper {

    @Mapping(target = "product", source = "productId", qualifiedByName = "mapUuidToProduct")
    UserTransaction toUserTransaction(UserTransactionRequest request);
}
