package kz.don.todoapp.mappers;

import kz.don.todoapp.dto.response.UserResponse;
import kz.don.todoapp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse toUserResponse(User user);
    List<UserResponse> toListUserResponse(List<User> users);
}
