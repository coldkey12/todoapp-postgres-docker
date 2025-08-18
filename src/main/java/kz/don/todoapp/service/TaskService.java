package kz.don.todoapp.service;

import jakarta.persistence.EntityNotFoundException;
import kz.don.todoapp.audit.AuditActionEnum;
import kz.don.todoapp.audit.AuditLog;
import kz.don.todoapp.dto.request.TaskRequest;
import kz.don.todoapp.dto.response.TaskResponse;
import kz.don.todoapp.entity.Task;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.enums.RoleEnum;
import kz.don.todoapp.enums.StatusEnum;
import kz.don.todoapp.repository.TaskRepository;
import kz.don.todoapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<TaskResponse> getUserTasks(StatusEnum status) {
        User currentUser = getCurrentUser();

        List<Task> tasks = (status == null)
                ? taskRepository.findByUserOrderByCreatedAtDesc(currentUser)
                : taskRepository.findByUserAndStatusOrderByCreatedAtDesc(currentUser, status);

        log.info("Retrieved {} tasks for user: {}", tasks.size(), currentUser.getUsername());

        auditService.saveAuditLog(
                AuditLog.builder()
                        .action(AuditActionEnum.READ)
                        .userId(currentUser.getId())
                        .details("User retrieved tasks successfully")
                        .build()
        );

        return tasks.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse createTask(TaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : StatusEnum.TODO)
                .user(currentUser)
                .build();

        auditService.saveAuditLog(
                AuditLog.builder()
                        .action(AuditActionEnum.CREATE)
                        .userId(currentUser.getId())
                        .details("User created a task successfully")
                        .build()
        );

        task = taskRepository.save(task);
        log.info("Task created: {}", task.getId());
        return mapToResponse(task);
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

        auditService.saveAuditLog(
                AuditLog.builder()
                        .action(AuditActionEnum.UPDATE)
                        .userId(getCurrentUser().getId())
                        .details("User updated a task successfully")
                        .build()
        );

        task = taskRepository.save(task);
        log.info("Task updated: {}", task.getId());
        return mapToResponse(task);
    }

    public void deleteTask(UUID id) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        checkTaskOwnership(task);

        auditService.saveAuditLog(
                AuditLog.builder()
                        .action(AuditActionEnum.CREATE)
                        .userId(getCurrentUser().getId())
                        .details("User deleted a task successfully")
                        .build()
        );

        taskRepository.delete(task);
        log.info("Task deleted: {}", id);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void checkTaskOwnership(Task task) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == RoleEnum.ADMIN) {
            return;
        }

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You don't have permission to modify this task");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

}