package kz.don.todoapp.mappers;

import kz.don.todoapp.dto.response.WalletResponse;
import kz.don.todoapp.entity.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    WalletResponse toWalletResponse(Wallet wallet);

}
