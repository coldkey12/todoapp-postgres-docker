package kz.don.todoapp.controller.user;

import kz.don.todoapp.dto.response.WalletResponse;
import kz.don.todoapp.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<WalletResponse> getWalletInfo() {
        return ResponseEntity.ok(walletService.getWalletInfo());
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet() {
        return ResponseEntity.ok(walletService.createWallet());
    }

}
