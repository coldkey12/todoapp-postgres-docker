package kz.don.todoapp.service.cars;

import kz.don.todoapp.dto.cars.BMWDTO;
import kz.don.todoapp.entity.cars.BMW;
import kz.don.todoapp.mappers.cars.BMWMapper;
import kz.don.todoapp.repository.cars.BMWRepository;
import kz.don.todoapp.service.cars.base.CarService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BMWService extends CarService<BMW, BMWRepository> {

    private final BMWRepository bmwRepository;
    private final BMWMapper bmwmapper;

    public BMWService(BMWRepository repository, BMWRepository bmwRepository, BMWMapper bmwmapper) {
        super(repository);
        this.bmwRepository = bmwRepository;
        this.bmwmapper = bmwmapper;
    }

    public BMWDTO getBmw(UUID uuid){
        BMW bmw = bmwRepository.findById(uuid).orElse(null);
        assert bmw != null;
        return bmwmapper.toDto(bmw);
    }
}
