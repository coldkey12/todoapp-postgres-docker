package kz.don.todoapp.mappers;

import kz.don.todoapp.dto.response.UserResponse;
import kz.don.todoapp.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    List<UserResponse> toListUserResponse(List<User> users);
}
