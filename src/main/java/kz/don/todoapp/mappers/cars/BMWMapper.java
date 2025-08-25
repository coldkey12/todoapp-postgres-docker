package kz.don.todoapp.mappers.cars;

import kz.don.todoapp.dto.cars.BMWDTO;
import kz.don.todoapp.entity.cars.BMW;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BMWMapper {

    BMWDTO toDto(BMW bmw);

    BMW toEntity(BMWDTO bmwDTO);
}
