package kz.don.todoapp.entity.cars.base;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;


@Data
@MappedSuperclass
public abstract class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String model;
}
