package kz.don.todoapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDTO {

    private String userId;

    private String productId;

    private String transactionType;

    private double sum;
    private int quantity;

    private double pricePerPiece;
}
