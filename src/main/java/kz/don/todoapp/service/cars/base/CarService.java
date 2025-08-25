package kz.don.todoapp.service.cars.base;

import kz.don.todoapp.entity.cars.base.Car;
import kz.don.todoapp.repository.cars.base.CarRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class CarService<E extends Car, R extends CarRepository<E>> {

    protected final R repository;

    public void save(E entity) {
        repository.save(entity);
    }
}
