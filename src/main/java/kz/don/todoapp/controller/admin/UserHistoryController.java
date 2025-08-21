package kz.don.todoapp.controller.admin;

import kz.don.todoapp.entity.User;
import kz.don.todoapp.repository.UserRevisionRepository;
import org.springframework.data.history.Revision;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/user-history")
public class UserHistoryController {

    private final UserRevisionRepository userRevisionRepository;

    public UserHistoryController(UserRevisionRepository userRevisionRepository) {
        this.userRevisionRepository = userRevisionRepository;
    }

    @GetMapping("/{userId}")
    public List<User> getUserHistory(@PathVariable String userId) {
        return userRevisionRepository.findRevisions(UUID.fromString(userId))
                .getContent()
                .stream()
                .map(Revision::getEntity)
                .toList();
    }

}
