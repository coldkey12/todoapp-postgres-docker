package kz.don.todoapp.entity;

import jakarta.persistence.*;
import kz.don.todoapp.enums.CurrencyEnum;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
@EqualsAndHashCode(exclude = "user")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private double balance;

    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;

    @OneToOne(fetch = FetchType.EAGER)
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
