package com.bookstore.online_bookstore_backend;

import com.bookstore.online_bookstore_backend.entity.ERole;
import com.bookstore.online_bookstore_backend.entity.Role;
import com.bookstore.online_bookstore_backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@SpringBootApplication
public class OnlineBookstoreBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineBookstoreBackendApplication.class, args);
	}

	@Bean
	@Order(1) // 确保角色初始化最先执行
	CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {
			System.out.println("========================================");
			System.out.println("  检查并初始化角色...");
			System.out.println("========================================");
			if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_USER));
				System.out.println("✓ 已创建 ROLE_USER");
			}
			if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_ADMIN));
				System.out.println("✓ 已创建 ROLE_ADMIN");
			}
			if (roleRepository.findByName(ERole.ROLE_MODERATOR).isEmpty()) {
				roleRepository.save(new Role(ERole.ROLE_MODERATOR));
				System.out.println("✓ 已创建 ROLE_MODERATOR");
			}
			System.out.println("✓ 角色初始化完成");
			System.out.println("========================================");
		};
	}
}
