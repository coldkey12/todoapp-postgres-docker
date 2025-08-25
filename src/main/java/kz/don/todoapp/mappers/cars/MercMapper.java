package kz.don.todoapp.mappers.cars;

import kz.don.todoapp.dto.cars.MercDTO;
import kz.don.todoapp.entity.cars.MercedesBenz;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MercMapper {

    MercDTO toDto(MercedesBenz mercedesBenz);

    MercedesBenz toEntity(MercDTO mercDTO);

}
