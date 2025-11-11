package com.raoudate.GestionDeTri;

import com.raoudate.GestionDeTri.auth.AuthenticationService;
import com.raoudate.GestionDeTri.auth.RegistrationRequest;
import com.raoudate.GestionDeTri.model.Role;
import com.raoudate.GestionDeTri.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.CommandLineRunner;
import static com.raoudate.GestionDeTri.Enum.RoleType.ADMIN;
import static com.raoudate.GestionDeTri.Enum.RoleType.SUPERVISEUR;
import static com.raoudate.GestionDeTri.Enum.RoleType.OPERATEUR;

// use RegistrationRequest.builder() directly

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
public class GestionDeTriApplication {

	private static final Logger log = LoggerFactory.getLogger(GestionDeTriApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GestionDeTriApplication.class, args);
	}

	@Bean
	public CommandLineRunner Runner(
			AuthenticationService service,  
			RoleRepository roleRepository, 
			com.raoudate.GestionDeTri.repository.UserRepository userRepository,
			com.raoudate.GestionDeTri.services.api.RoleService roleService) {
	return args -> {

		// create roles with 'ROLE_' prefix because AuthenticationService expects 'ROLE_ADMIN', etc.
		String adminRoleName = "ROLE_" + ADMIN.name();
		if (roleRepository.findByName(adminRoleName).isEmpty()) {
			Role r = new Role();
			r.setName(adminRoleName);
			roleRepository.save(r);
			log.info("Created role {}", adminRoleName);
		}
		String supRoleName = "ROLE_" + SUPERVISEUR.name();
		if (roleRepository.findByName(supRoleName).isEmpty()) {
			Role r = new Role();
			r.setName(supRoleName);
			roleRepository.save(r);
			log.info("Created role {}", supRoleName);
		}
		
		// Créer le rôle OPERATEUR
		String operateurRoleName = "ROLE_" + OPERATEUR.name();
		if (roleRepository.findByName(operateurRoleName).isEmpty()) {
			Role r = new Role();
			r.setName(operateurRoleName);
			roleRepository.save(r);
			log.info("Created role {}", operateurRoleName);
		}
		
		// Initialiser les permissions des rôles au démarrage
		try {
			roleService.initializeRolePermissions();
			log.info("✅ Permissions des rôles initialisées avec succès");
		} catch (Exception e) {
			log.error("❌ Erreur lors de l'initialisation des permissions: {}", e.getMessage());
		}
		
		var admin = RegistrationRequest.builder()
			.firstname("Raoudate")
			.lastname("BATCHA")
			.email("raoudatebatcha@gmail.com")
			.password("Admin123!")
			.role(ADMIN)
			.build();
		if (userRepository.findByEmail(admin.getEmail()).isEmpty()) {
			try {
				String adminToken = service.register(admin);
				log.info("Admin token: {}", adminToken);
			} catch (jakarta.mail.MessagingException e) {
				log.warn("Failed to send admin activation email at startup: {}", e.getMessage());
			}
		} else {
			log.info("Admin user already exists: {}", admin.getEmail());
		}

		var superviseur = RegistrationRequest.builder()
			.firstname("superviseur")
			.lastname("BATCHA")
			.email("justraodath@gmail.com")
			.password("Admin123!")
			.role(SUPERVISEUR)
			.build();
		if (userRepository.findByEmail(superviseur.getEmail()).isEmpty()) {
			try {
				String supToken = service.register(superviseur);
				log.info("Superviseur token: {}", supToken);
			} catch (jakarta.mail.MessagingException e) {
				log.warn("Failed to send superviseur activation email at startup: {}", e.getMessage());
			}
		} else {
			log.info("Superviseur user already exists: {}", superviseur.getEmail());
		}
	};

	}

}
