package kz.don.todoapp.service;

import kz.don.todoapp.dto.response.WalletResponse;
import kz.don.todoapp.mappers.WalletMapper;
import kz.don.todoapp.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;
    private final WalletMapper walletMapper;

    public WalletResponse getWalletInfo() {
        return walletMapper.toWalletResponse(walletRepository.getWalletInfo(userService.getCurrentUser().getId()));
    }
}
