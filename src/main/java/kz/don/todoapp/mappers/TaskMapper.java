package kz.don.todoapp.mappers;

import kz.don.todoapp.dto.request.TaskRequest;
import kz.don.todoapp.dto.response.TaskResponse;
import kz.don.todoapp.entity.Task;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    List<TaskResponse> toListTaskResponse(List<Task> tasks);

    Task toTask(TaskRequest taskRequest);

    TaskResponse toTaskResponse(Task task);
}
