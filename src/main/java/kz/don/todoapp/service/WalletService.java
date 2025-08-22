package kz.don.todoapp.service;

import kz.don.todoapp.dto.response.WalletResponse;
import kz.don.todoapp.entity.Wallet;
import kz.don.todoapp.enums.CurrencyEnum;
import kz.don.todoapp.mappers.WalletMapper;
import kz.don.todoapp.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;
    private final WalletMapper walletMapper;

    public WalletResponse getWalletInfo() {
        return walletMapper.toWalletResponse(walletRepository.getWalletInfo(userService.getCurrentUser().getId()));
    }

    @Transactional
    public WalletResponse createWallet() {

        if (walletRepository.existsByUserId(userService.getCurrentUser().getId())) {
            throw new IllegalStateException("Wallet already exists for this user");
        }

        Wallet wallet = Wallet.builder()
                .user(userService.getCurrentUser())
                .balance(0.0)
                .currency(CurrencyEnum.KZT)
                .build();

        return walletMapper.toWalletResponse(walletRepository.save(wallet));
    }
}
