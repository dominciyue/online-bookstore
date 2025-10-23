package com.bookstore.online_bookstore_backend;

import com.bookstore.online_bookstore_backend.entity.ERole;
import com.bookstore.online_bookstore_backend.entity.Role;
import com.bookstore.online_bookstore_backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OnlineBookstoreBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineBookstoreBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {
			System.out.println("Checking and initializing roles...");
			if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_USER));
				System.out.println("Initialized ROLE_USER");
			}
			if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_ADMIN));
				System.out.println("Initialized ROLE_ADMIN");
			}
			if (roleRepository.findByName(ERole.ROLE_MODERATOR).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_MODERATOR));
				System.out.println("Initialized ROLE_MODERATOR");
			}
			System.out.println("Role initialization complete.");
		};
	}
}
