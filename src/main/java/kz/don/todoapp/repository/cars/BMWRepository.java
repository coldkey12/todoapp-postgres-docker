package kz.don.todoapp.repository.cars;

import kz.don.todoapp.entity.cars.BMW;
import kz.don.todoapp.repository.cars.base.CarRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BMWRepository extends CarRepository<BMW> {

}
