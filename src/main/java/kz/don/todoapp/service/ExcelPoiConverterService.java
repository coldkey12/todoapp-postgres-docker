package kz.don.todoapp.service;

import kz.don.todoapp.entity.User;
import kz.don.todoapp.enums.RoleEnum;
import kz.don.todoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExcelPoiConverterService {

    private final UserService userService;
    private final UserRepository userRepository;

    public void loadData(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                Cell idCell = row.getCell(0);
                Cell balanceCell = row.getCell(1);

                if (idCell == null || balanceCell == null) {
                    continue;
                }

                try {
                    UUID userId = UUID.fromString(idCell.getStringCellValue());
                    double balanceToAdd = balanceCell.getNumericCellValue();

                    User user = userService.findById(userId);
                    if (user != null && user.getWallet() != null) {
                        user.getWallet().setBalance(user.getWallet().getBalance() + balanceToAdd);
                        userRepository.save(user);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid data format in row " + row.getRowNum() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] exportData() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("todoapp");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"username", "balance", "uuid"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            List<User> users = userRepository.findAll();

            int rowNum = 1;

            for (User user : users) {

                if (user.getRole() == RoleEnum.ADMIN) {
                    continue;
                }

                Row row = sheet.createRow(rowNum++);

                Cell usernameCell = row.createCell(0);
                usernameCell.setCellValue(user.getUsername());

                Cell balanceCell = row.createCell(1);

                if (user.getWallet() != null) {
                    balanceCell.setCellValue(user.getWallet().getBalance());
                } else {
                    balanceCell.setCellValue(0.0);
                }

                Cell idCell = row.createCell(2);
                idCell.setCellValue(user.getId().toString());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("eto chto bruh ", e);
        }
    }
}
