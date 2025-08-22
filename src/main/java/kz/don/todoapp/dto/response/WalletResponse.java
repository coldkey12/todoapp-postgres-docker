package kz.don.todoapp.dto.response;

import kz.don.todoapp.enums.CurrencyEnum;
import lombok.Data;

@Data
public class WalletResponse {

    private double balance;
    private CurrencyEnum currency;

}
