package kz.don.todoapp.repository.cars.base;

import kz.don.todoapp.entity.cars.base.Car;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CarRepository<E extends Car> extends CrudRepository<E, UUID> {
    @Override
    <S extends E> S save(S entity);
}
