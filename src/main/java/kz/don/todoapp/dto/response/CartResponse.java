package kz.don.todoapp.dto.response;

import kz.don.todoapp.dto.TransactionDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {

    private String userId;
    private List<TransactionDTO> transactionDTOS;
    private String IIN;
    private String firstName;
    private String secondName;
    private String patronymic;
    private String address;
    private String number;
    private Boolean isApproved;
}
