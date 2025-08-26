package kz.don.todoapp.repository.cars;

import kz.don.todoapp.entity.cars.MercedesBenz;
import kz.don.todoapp.repository.cars.base.CarRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

@Repository
@RedisHash
public interface MercedesBenzRepository extends CarRepository<MercedesBenz> {
}
