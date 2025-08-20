package kz.don.todoapp.dto.request;

import lombok.Data;


@Data
public class UserTransactionRequest {
    private String productId;
    private int quantity;
}
