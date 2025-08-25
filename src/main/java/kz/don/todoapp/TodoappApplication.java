package kz.don.todoapp;

import kz.don.todoapp.entity.User;
import kz.don.todoapp.enums.RoleEnum;
import kz.don.todoapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.envers.repository.config.EnableEnversRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableEnversRepositories
@EnableCaching
public class TodoappApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodoappApplication.class, args);
	}

	// TODO remove hardcode later
	@Bean
	CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			if (userRepository.findByUsername("admin").isEmpty()) {
				User admin = User.builder()
						.username("admin")
						.password(encoder.encode("12070107Don@"))
						.role(RoleEnum.ADMIN)
						.enabled(true)
						.build();
				userRepository.save(admin);
			}
		};
	}
}
