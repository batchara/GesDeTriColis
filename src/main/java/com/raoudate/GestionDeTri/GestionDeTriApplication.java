package com.raoudate.GestionDeTri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.CommandLineRunner;
import com.raoudate.GestionDeTri.repository.RoleRepository;
import com.raoudate.GestionDeTri.model.Role;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class GestionDeTriApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionDeTriApplication.class, args);
	}

	@Bean
	public CommandLineRunner Runner(RoleRepository roleRepository) {
		return args -> {

			if(roleRepository.findByName("ROLE_USER").isEmpty()) {
				roleRepository.save(
						Role.builder().name("ROLE_USER").build()
				);
			}

		};
	}

}
