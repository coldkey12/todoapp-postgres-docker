package kz.don.todoapp.service;

import jakarta.persistence.EntityNotFoundException;
import kz.don.todoapp.dto.request.TaskRequest;
import kz.don.todoapp.dto.response.TaskResponse;
import kz.don.todoapp.entity.Task;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.enums.RoleEnum;
import kz.don.todoapp.enums.StatusEnum;
import kz.don.todoapp.mappers.TaskMapper;
import kz.don.todoapp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskMapper taskMapper;

    public List<TaskResponse> getUserTasks(StatusEnum status) {
        User currentUser = userService.getCurrentUser();

        List<Task> tasks = (status == null)
                ? taskRepository.findByUserOrderByCreatedAtDesc(currentUser)
                : taskRepository.findByUserAndStatusOrderByCreatedAtDesc(currentUser, status);

        log.info("Retrieved {} tasks for user: {}", tasks.size(), currentUser.getUsername());

        return taskMapper.toListTaskResponse(tasks);
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = taskMapper.toTask(request);

        User currentUser = userService.getCurrentUser();
        task.setUser(currentUser);

        task = taskRepository.save(task);
        log.info("Task created: {}", task.getId());

        return taskMapper.toTaskResponse(task);
    }

    public TaskResponse updateTask(UUID id, TaskRequest request) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        checkTaskOwnership(task);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        task = taskRepository.save(task);
        log.info("Task updated: {}", task.getId());
        return taskMapper.toTaskResponse(task);
    }

    public void deleteTask(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        checkTaskOwnership(task);

        taskRepository.delete(task);
        log.info("Task deleted: {}", id);
    }

    private void checkTaskOwnership(Task task) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == RoleEnum.ADMIN) {
            return;
        }

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to modify this task");
        }
    }

}