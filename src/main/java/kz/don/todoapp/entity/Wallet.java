package kz.don.todoapp.entity;

import jakarta.persistence.*;
import kz.don.todoapp.enums.CurrencyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private double balance;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;

    @OneToOne(fetch = FetchType.EAGER)
    private User user;
}
