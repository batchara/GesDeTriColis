package com.raoudate.GestionDeTri.config;

import com.raoudate.GestionDeTri.enums.StatutColis;
import com.raoudate.GestionDeTri.model.*;
import com.raoudate.GestionDeTri.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AgenceRepository agenceRepository;
    private final RoleRepository roleRepository;
    private final ColisRepository colisRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // Créer les rôles s'ils n'existent pas
        Role roleAdmin = roleRepository.findByName("ADMIN").orElseGet(() -> 
            roleRepository.save(Role.builder().name("ADMIN").build())
        );
        Role roleOp = roleRepository.findByName("OPERATEUR").orElseGet(() -> 
            roleRepository.save(Role.builder().name("OPERATEUR").build())
        );
        Role roleSup = roleRepository.findByName("SUPERVISEUR").orElseGet(() -> 
            roleRepository.save(Role.builder().name("SUPERVISEUR").build())
        );

        // Créer les agences
        if (agenceRepository.count() == 0) {
            createAgences();
        }

        // Créer les utilisateurs
        if (userRepository.count() == 0) {
            createUsers(roleAdmin, roleOp, roleSup);
        }

        // Créer les colis de test
        if (colisRepository.count() == 0) {
            createColis();
        }

        log.info("Data initialization completed!");
    }

    private void createAgences() {
        log.info("Creating test agences...");
        String[][] agencesData = {
                {"AG001", "Lomé Centre", "Maritime/Golfe", "456"},
                {"AG002", "Sokodé", "Centrale", "298"},
                {"AG003", "Kara", "Kara", "234"},
                {"AG004", "Atakpamé", "Plateaux", "189"},
                {"AG005", "Dapaong", "Savanes", "124"},
                {"AG006", "Tsévié", "Maritime/Golfe", "167"},
                {"AG007", "Koforidua", "Plateaux", "145"},
        };

        for (String[] data : agencesData) {
            Agences agence = Agences.builder()
                    .code(data[0])
                    .label(data[1])
                    .region(data[2])
                    .codeBureau(data[3])
                    .tel("022" + (int)(Math.random() * 1000000))
                    .email(data[0].toLowerCase() + "@postetogo.tg")
                    .adresseComplete("Rue " + data[1])
                    .latitude(new BigDecimal("6.1256"))
                    .longitude(new BigDecimal("1.2317"))
                    .status("ACTIVE")
                    .isActive(true)
                    .build();
            agenceRepository.save(agence);
        }
        log.info(" {} agences created", agencesData.length);
    }

    private void createUsers(Role roleAdmin, Role roleOp, Role roleSup) {
        log.info("Creating test users...");

        // Admin user
        Admin admin = new Admin();
        admin.setNom("System");
        admin.setPrenom("Admin");
        admin.setEmail("admin@postetogo.tg");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEnabled(true);
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(roleAdmin);
        admin.setRoles(adminRoles);
        userRepository.save(admin);

        // Superviseur user
        Superviseur superviseur = new Superviseur();
        superviseur.setNom("System");
        superviseur.setPrenom("Superviseur");
        superviseur.setEmail("superviseur@postetogo.tg");
        superviseur.setPassword(passwordEncoder.encode("sup123"));
        superviseur.setEnabled(true);
        Set<Role> supRoles = new HashSet<>();
        supRoles.add(roleSup);
        superviseur.setRoles(supRoles);
        userRepository.save(superviseur);

        // Operateur users
        String[] operateurs = {"op1", "op2", "op3"};
        for (String opName : operateurs) {
            Operateur operateur = new Operateur();
            operateur.setNom(opName.toUpperCase());
            operateur.setPrenom("Operateur");
            operateur.setEmail(opName + "@postetogo.tg");
            operateur.setPassword(passwordEncoder.encode("op123"));
            operateur.setEnabled(true);
            Set<Role> opRoles = new HashSet<>();
            opRoles.add(roleOp);
            operateur.setRoles(opRoles);
            userRepository.save(operateur);
        }

        log.info(" Users created (admin, superviseur, 3 operateurs)");
    }

    private void createColis() {
        log.info("Creating test colis...");

        // Trouver un opérateur
        User opUser = userRepository.findAll().stream()
                .filter(u -> u instanceof Operateur)
                .findFirst()
                .orElse(null);
        
        Agences agence = agenceRepository.findAll().stream().findFirst().orElse(null);

        if (opUser == null || agence == null) {
            log.warn("Cannot create colis: missing operateur or agence");
            return;
        }

        StatutColis[] statuts = {
                StatutColis.AFFECTE,
                StatutColis.EXPEDIE,
                StatutColis.RECEPTIONNE,
                StatutColis.LIVRE,
                StatutColis.RETOUR
        };

        for (int i = 0; i < 50; i++) {
            StatutColis statut = statuts[i % statuts.length];
            Colis colis = Colis.builder()
                    .codeSuivi("TRI" + String.format("%06d", i + 1))
                    .nomDest("Client " + i)
                    .telDest("022" + (int)(Math.random() * 1000000))
                    .adresseDest("Adresse " + i)
                    .poids(new BigDecimal((Math.random() * 10 + 0.5)))
                    .statut(statut)
                    .dateEnvoi(Instant.now())
                    .operateur((Operateur) opUser)
                    .agenceAffectee(agence)
                    .build();
            colisRepository.save(colis);
        }

        log.info(" 50 test colis created");
    }
}
