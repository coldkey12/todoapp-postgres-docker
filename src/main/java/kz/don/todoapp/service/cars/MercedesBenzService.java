package kz.don.todoapp.service.cars;

import kz.don.todoapp.dto.cars.MercDTO;
import kz.don.todoapp.entity.cars.MercedesBenz;
import kz.don.todoapp.mappers.cars.MercMapper;
import kz.don.todoapp.repository.cars.MercedesBenzRepository;
import kz.don.todoapp.service.cars.base.CarService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MercedesBenzService extends CarService<MercedesBenz, MercedesBenzRepository> {

    private final MercedesBenzRepository mercedesBenzRepository;
    private final MercMapper mercMapper;

    public MercedesBenzService(MercedesBenzRepository repository, MercedesBenzRepository mercedesBenzRepository, MercMapper mercMapper) {
        super(repository);
        this.mercedesBenzRepository = mercedesBenzRepository;
        this.mercMapper = mercMapper;
    }

    public MercDTO mileage(UUID uuid){
        MercedesBenz mercedesBenz = mercedesBenzRepository.findById(uuid).orElse(null);
        assert mercedesBenz != null;
        return mercMapper.toDto(mercedesBenz);
    }
}
