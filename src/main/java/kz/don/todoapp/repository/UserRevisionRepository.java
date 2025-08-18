package kz.don.todoapp.repository;

import kz.don.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.UUID;

public interface UserRevisionRepository extends JpaRepository<User, UUID>, RevisionRepository<User, UUID, Integer> {
}
