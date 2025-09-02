package kz.don.todoapp.repository;

import kz.don.todoapp.entity.Task;
import kz.don.todoapp.entity.User;
import kz.don.todoapp.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RedisHash
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUserOrderByCreatedAtDesc(User currentUser);

    List<Task> findByUserAndStatusOrderByCreatedAtDesc(User currentUser, StatusEnum status);
}
