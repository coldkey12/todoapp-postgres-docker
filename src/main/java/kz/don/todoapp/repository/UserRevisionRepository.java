package kz.don.todoapp.repository;

import kz.don.todoapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRevisionRepository extends JpaRepository<User, UUID>, RevisionRepository<User, UUID, Integer> {
}
