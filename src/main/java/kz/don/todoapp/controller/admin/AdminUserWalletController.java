package kz.don.todoapp.controller.admin;

import kz.don.todoapp.service.ExcelPoiConverterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/wallet")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminUserWalletController {

    private final ExcelPoiConverterService excelPoiConverterService;

    @PostMapping("/excel")
    public ResponseEntity<String> loadData(@RequestBody MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        excelPoiConverterService.loadData(file);
        return ResponseEntity.ok("Data loaded successfully");
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportData() {
        byte[] excelData = excelPoiConverterService.exportData();
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=wallets.xlsx")
                .body(excelData);
    }
}
