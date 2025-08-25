package kz.don.todoapp.entity.cars;

import jakarta.persistence.Entity;
import kz.don.todoapp.entity.cars.base.Car;
import lombok.Data;

@Data
@Entity
public class BMW extends Car {

    private boolean isMSeries;
}
