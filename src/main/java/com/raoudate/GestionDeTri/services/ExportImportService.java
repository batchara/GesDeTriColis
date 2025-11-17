package com.raoudate.GestionDeTri.services;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.raoudate.GestionDeTri.model.*;
import com.raoudate.GestionDeTri.repository.UserRepository;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import com.raoudate.GestionDeTri.repository.ColisRepository;
import com.raoudate.GestionDeTri.repository.RoleRepository;
import com.raoudate.GestionDeTri.Enum.StatutColis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportImportService {

    private final UserRepository userRepository;
    private final AgenceRepository agenceRepository;
    private final ColisRepository colisRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== EXPORT UTILISATEURS ====================

    public ByteArrayInputStream exportUsersToExcel() {
        List<User> users = userRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Utilisateurs");

            // En-têtes
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Prénom", "Nom", "Email", "Téléphone", "Date de naissance", "Actif", "Rôles"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Données
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getPrenom());
                row.createCell(2).setCellValue(user.getNom());
                row.createCell(3).setCellValue(user.getEmail());
                row.createCell(4).setCellValue(user.getNumTel() != null ? user.getNumTel() : "");
                row.createCell(5).setCellValue(user.getDateNaissance() != null ? user.getDateNaissance().toString() : "");
                row.createCell(6).setCellValue(user.isEnabled() ? "Oui" : "Non");
                
                String roles = user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining(", "));
                row.createCell(7).setCellValue(roles);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Erreur lors de l'export Excel des utilisateurs", e);
            throw new RuntimeException("Erreur lors de l'export Excel", e);
        }
    }

    public ByteArrayInputStream exportUsersToCSV() {
        List<User> users = userRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // En-têtes
            String[] headers = {"ID", "Prénom", "Nom", "Email", "Téléphone", "Date de naissance", "Actif", "Rôles"};
            csvWriter.writeNext(headers);

            // Données
            for (User user : users) {
                String[] data = {
                        user.getId().toString(),
                        user.getPrenom(),
                        user.getNom(),
                        user.getEmail(),
                        user.getNumTel() != null ? user.getNumTel() : "",
                        user.getDateNaissance() != null ? user.getDateNaissance().toString() : "",
                        user.isEnabled() ? "Oui" : "Non",
                        user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", "))
                };
                csvWriter.writeNext(data);
            }

            csvWriter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Erreur lors de l'export CSV des utilisateurs", e);
            throw new RuntimeException("Erreur lors de l'export CSV", e);
        }
    }

    // ==================== IMPORT UTILISATEURS ====================

    public Map<String, Object> importUsersFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row row = rows.next();
                try {
                    String firstname = getCellValue(row.getCell(1));
                    String lastname = getCellValue(row.getCell(2));
                    String email = getCellValue(row.getCell(3));
                    String phone = getCellValue(row.getCell(4));
                    String dateNaissanceStr = getCellValue(row.getCell(5));
                    String rolesStr = getCellValue(row.getCell(7));

                    // Vérifier si l'utilisateur existe déjà
                    if (userRepository.findByEmail(email).isPresent()) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + ": Email déjà existant - " + email);
                        errorCount++;
                        continue;
                    }

                    User user = User.builder()
                            .prenom(firstname)
                            .nom(lastname)
                            .email(email)
                            .numTel(phone)
                            .password(passwordEncoder.encode("Password123!")) // Mot de passe par défaut
                            .accountLocked(false)
                            .enabled(true)
                            .build();

                    if (dateNaissanceStr != null && !dateNaissanceStr.isEmpty()) {
                        user.setDateNaissance(LocalDate.parse(dateNaissanceStr));
                    }

                    // Attribuer les rôles
                    if (rolesStr != null && !rolesStr.isEmpty()) {
                        Set<Role> roles = new HashSet<>();
                        String[] roleNames = rolesStr.split(",");
                        for (String roleName : roleNames) {
                            roleRepository.findByName(roleName.trim()).ifPresent(roles::add);
                        }
                        user.setRoles(roles);
                    }

                    userRepository.save(user);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Ligne " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'import Excel", e);
            throw new RuntimeException("Erreur lors de l'import Excel", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("errors", errorCount);
        result.put("errorDetails", errors);
        return result;
    }

    public Map<String, Object> importUsersFromCSV(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            // Skip header
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    String firstname = row[1];
                    String lastname = row[2];
                    String email = row[3];
                    String phone = row.length > 4 ? row[4] : "";
                    String dateNaissanceStr = row.length > 5 ? row[5] : "";
                    String rolesStr = row.length > 7 ? row[7] : "";

                    if (userRepository.findByEmail(email).isPresent()) {
                        errors.add("Ligne " + (i + 1) + ": Email déjà existant - " + email);
                        errorCount++;
                        continue;
                    }

                    User user = User.builder()
                            .prenom(firstname)
                            .nom(lastname)
                            .email(email)
                            .numTel(phone)
                            .password(passwordEncoder.encode("Password123!"))
                            .accountLocked(false)
                            .enabled(true)
                            .build();

                    if (dateNaissanceStr != null && !dateNaissanceStr.isEmpty()) {
                        user.setDateNaissance(LocalDate.parse(dateNaissanceStr));
                    }

                    if (rolesStr != null && !rolesStr.isEmpty()) {
                        Set<Role> roles = new HashSet<>();
                        String[] roleNames = rolesStr.split(",");
                        for (String roleName : roleNames) {
                            roleRepository.findByName(roleName.trim()).ifPresent(roles::add);
                        }
                        user.setRoles(roles);
                    }

                    userRepository.save(user);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Ligne " + (i + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (IOException | CsvException e) {
            log.error("Erreur lors de l'import CSV", e);
            throw new RuntimeException("Erreur lors de l'import CSV", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("errors", errorCount);
        result.put("errorDetails", errors);
        return result;
    }

    // ==================== EXPORT AGENCES ====================

    public ByteArrayInputStream exportAgencesToExcel() {
        List<Agences> agences = agenceRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Agences");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Code", "Label", "Email", "Téléphone", "Région", "Adresse"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowNum = 1;
            for (Agences agence : agences) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(agence.getId());
                row.createCell(1).setCellValue(agence.getCode() != null ? agence.getCode() : "");
                row.createCell(2).setCellValue(agence.getLabel() != null ? agence.getLabel() : "");
                row.createCell(3).setCellValue(agence.getEmail() != null ? agence.getEmail() : "");
                row.createCell(4).setCellValue(agence.getTel() != null ? agence.getTel() : "");
                row.createCell(5).setCellValue(agence.getRegion() != null ? agence.getRegion() : "");
                row.createCell(6).setCellValue(agence.getAdresseComplete() != null ? agence.getAdresseComplete() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Erreur lors de l'export Excel des agences", e);
            throw new RuntimeException("Erreur lors de l'export Excel", e);
        }
    }

    public ByteArrayInputStream exportAgencesToCSV() {
        List<Agences> agences = agenceRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            String[] headers = {"ID", "Code", "Label", "Email", "Téléphone", "Région", "Adresse"};
            csvWriter.writeNext(headers);

            for (Agences agence : agences) {
                String[] data = {
                        agence.getId().toString(),
                        agence.getCode() != null ? agence.getCode() : "",
                        agence.getLabel() != null ? agence.getLabel() : "",
                        agence.getEmail() != null ? agence.getEmail() : "",
                        agence.getTel() != null ? agence.getTel() : "",
                        agence.getRegion() != null ? agence.getRegion() : "",
                        agence.getAdresseComplete() != null ? agence.getAdresseComplete() : ""
                };
                csvWriter.writeNext(data);
            }

            csvWriter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Erreur lors de l'export CSV des agences", e);
            throw new RuntimeException("Erreur lors de l'export CSV", e);
        }
    }

    // ==================== EXPORT COLIS ====================

    public ByteArrayInputStream exportColisToExcel() {
        List<Colis> colisList = colisRepository.findAllActive();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Colis");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Code Suivi", "Poids (kg)", "Expéditeur", "Destinataire", 
                               "Tél. Destinataire", "Date Envoi", "Date Prévue", "Statut", "Agence"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (Colis colis : colisList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(colis.getId());
                row.createCell(1).setCellValue(colis.getCodeSuivi());
                row.createCell(2).setCellValue(colis.getPoids() != null ? colis.getPoids().doubleValue() : 0);
                row.createCell(3).setCellValue(colis.getNomExp() != null ? colis.getNomExp() : "");
                row.createCell(4).setCellValue(colis.getNomDest() != null ? colis.getNomDest() : "");
                row.createCell(5).setCellValue(colis.getTelDest() != null ? colis.getTelDest() : "");
                
                String dateEnvoi = colis.getDateEnvoi() != null ? 
                    formatter.format(colis.getDateEnvoi().atZone(ZoneId.systemDefault())) : "";
                row.createCell(6).setCellValue(dateEnvoi);
                
                String datePrevue = colis.getDatePrevue() != null ? 
                    formatter.format(colis.getDatePrevue().atZone(ZoneId.systemDefault())) : "";
                row.createCell(7).setCellValue(datePrevue);
                
                row.createCell(8).setCellValue(colis.getStatut() != null ? colis.getStatut().toString() : "");
                
                String agence = colis.getAgenceAffectee() != null ? colis.getAgenceAffectee().getLabel() : "";
                row.createCell(9).setCellValue(agence);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Erreur lors de l'export Excel des colis", e);
            throw new RuntimeException("Erreur lors de l'export Excel", e);
        }
    }

    public ByteArrayInputStream exportColisToCSV() {
        List<Colis> colisList = colisRepository.findAllActive();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            String[] headers = {"ID", "Code Suivi", "Poids (kg)", "Expéditeur", "Destinataire", 
                               "Tél. Destinataire", "Date Envoi", "Date Prévue", "Statut", "Agence"};
            csvWriter.writeNext(headers);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            for (Colis colis : colisList) {
                String dateEnvoi = colis.getDateEnvoi() != null ? 
                    formatter.format(colis.getDateEnvoi().atZone(ZoneId.systemDefault())) : "";
                String datePrevue = colis.getDatePrevue() != null ? 
                    formatter.format(colis.getDatePrevue().atZone(ZoneId.systemDefault())) : "";
                String agence = colis.getAgenceAffectee() != null ? colis.getAgenceAffectee().getLabel() : "";

                String[] data = {
                        colis.getId().toString(),
                        colis.getCodeSuivi(),
                        colis.getPoids() != null ? colis.getPoids().toString() : "0",
                        colis.getNomExp() != null ? colis.getNomExp() : "",
                        colis.getNomDest() != null ? colis.getNomDest() : "",
                        colis.getTelDest() != null ? colis.getTelDest() : "",
                        dateEnvoi,
                        datePrevue,
                        colis.getStatut() != null ? colis.getStatut().toString() : "",
                        agence
                };
                csvWriter.writeNext(data);
            }

            csvWriter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Erreur lors de l'export CSV des colis", e);
            throw new RuntimeException("Erreur lors de l'export CSV", e);
        }
    }

    // ==================== IMPORT AGENCES ====================

    public Map<String, Object> importAgencesFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row row = rows.next();
                try {
                    String code = getCellValue(row.getCell(1));
                    String label = getCellValue(row.getCell(2));
                    String email = getCellValue(row.getCell(3));
                    String tel = getCellValue(row.getCell(4));
                    String region = getCellValue(row.getCell(5));
                    String adresseCompleteStr = getCellValue(row.getCell(6));

                    // Vérifier si le code existe déjà
                    if (code != null && !code.isEmpty()) {
                        boolean exists = agenceRepository.findAll().stream()
                                .anyMatch(a -> code.equals(a.getCode()));
                        if (exists) {
                            errors.add("Ligne " + (row.getRowNum() + 1) + ": Code agence déjà existant - " + code);
                            errorCount++;
                            continue;
                        }
                    }

                    // Créer l'agence
                    Agences agence = new Agences();
                    agence.setCode(code);
                    agence.setLabel(label);
                    agence.setEmail(email);
                    agence.setTel(tel);
                    agence.setRegion(region);
                    agence.setAdresseComplete(adresseCompleteStr != null ? adresseCompleteStr : "");

                    agenceRepository.save(agence);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Ligne " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'import Excel des agences", e);
            throw new RuntimeException("Erreur lors de l'import Excel", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("errors", errorCount);
        result.put("errorDetails", errors);
        return result;
    }

    public Map<String, Object> importAgencesFromCSV(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            // Skip header
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    String code = row.length > 1 ? row[1] : "";
                    String label = row.length > 2 ? row[2] : "";
                    String email = row.length > 3 ? row[3] : "";
                    String tel = row.length > 4 ? row[4] : "";
                    String region = row.length > 5 ? row[5] : "";
                    String adresseCompleteStr = row.length > 6 ? row[6] : "";

                    // Vérifier si le code existe déjà
                    if (code != null && !code.isEmpty()) {
                        boolean exists = agenceRepository.findAll().stream()
                                .anyMatch(a -> code.equals(a.getCode()));
                        if (exists) {
                            errors.add("Ligne " + (i + 1) + ": Code agence déjà existant - " + code);
                            errorCount++;
                            continue;
                        }
                    }

                    // Créer l'agence
                    Agences agence = new Agences();
                    agence.setCode(code);
                    agence.setLabel(label);
                    agence.setEmail(email);
                    agence.setTel(tel);
                    agence.setRegion(region);
                    agence.setAdresseComplete(adresseCompleteStr != null ? adresseCompleteStr : "");

                    agenceRepository.save(agence);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Ligne " + (i + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (IOException | CsvException e) {
            log.error("Erreur lors de l'import CSV des agences", e);
            throw new RuntimeException("Erreur lors de l'import CSV", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("errors", errorCount);
        result.put("errorDetails", errors);
        return result;
    }

    // ==================== IMPORT COLIS ====================

    public Map<String, Object> importColisFromExcel(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header
            if (rows.hasNext()) rows.next();

            while (rows.hasNext()) {
                Row row = rows.next();
                try {
                    String codeSuivi = getCellValue(row.getCell(1));
                    String poidsStr = getCellValue(row.getCell(2));
                    String nomExp = getCellValue(row.getCell(3));
                    String nomDest = getCellValue(row.getCell(4));
                    String telDest = getCellValue(row.getCell(5));
                    String statutStr = getCellValue(row.getCell(8));

                    // Vérifier si le code de suivi existe déjà (parmi les colis actifs uniquement)
                    if (colisRepository.existsByCodeSuivi(codeSuivi)) {
                        errors.add("Ligne " + (row.getRowNum() + 1) + ": Code de suivi déjà existant - " + codeSuivi);
                        errorCount++;
                        continue;
                    }

                    // Construire le colis
                    Colis.ColisBuilder colisBuilder = Colis.builder()
                            .codeSuivi(codeSuivi)
                            .nomExp(nomExp)
                            .nomDest(nomDest)
                            .telDest(telDest);

                    // Poids
                    if (poidsStr != null && !poidsStr.isEmpty()) {
                        try {
                            colisBuilder.poids(java.math.BigDecimal.valueOf(Double.parseDouble(poidsStr)));
                        } catch (NumberFormatException e) {
                            errors.add("Ligne " + (row.getRowNum() + 1) + ": Poids invalide - " + poidsStr);
                        }
                    }

                    // Statut
                    if (statutStr != null && !statutStr.isEmpty()) {
                        try {
                            colisBuilder.statut(StatutColis.valueOf(statutStr));
                        } catch (IllegalArgumentException e) {
                            colisBuilder.statut(StatutColis.EN_ATTENTE);
                        }
                    } else {
                        colisBuilder.statut(StatutColis.EN_ATTENTE);
                    }

                    Colis colis = colisBuilder.build();
                    colisRepository.save(colis);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Ligne " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (IOException e) {
            log.error("Erreur lors de l'import Excel des colis", e);
            throw new RuntimeException("Erreur lors de l'import Excel", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("errors", errorCount);
        result.put("errorDetails", errors);
        return result;
    }

    public Map<String, Object> importColisFromCSV(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();
            // Skip header
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    String codeSuivi = row.length > 1 ? row[1] : "";
                    String poidsStr = row.length > 2 ? row[2] : "";
                    String nomExp = row.length > 3 ? row[3] : "";
                    String nomDest = row.length > 4 ? row[4] : "";
                    String telDest = row.length > 5 ? row[5] : "";
                    String statutStr = row.length > 8 ? row[8] : "";

                    // Vérifier si le code de suivi existe déjà (parmi les colis actifs uniquement)
                    if (colisRepository.existsByCodeSuivi(codeSuivi)) {
                        errors.add("Ligne " + (i + 1) + ": Code de suivi déjà existant - " + codeSuivi);
                        errorCount++;
                        continue;
                    }

                    // Construire le colis
                    Colis.ColisBuilder colisBuilder = Colis.builder()
                            .codeSuivi(codeSuivi)
                            .nomExp(nomExp)
                            .nomDest(nomDest)
                            .telDest(telDest);

                    // Poids
                    if (poidsStr != null && !poidsStr.isEmpty()) {
                        try {
                            colisBuilder.poids(java.math.BigDecimal.valueOf(Double.parseDouble(poidsStr)));
                        } catch (NumberFormatException e) {
                            errors.add("Ligne " + (i + 1) + ": Poids invalide - " + poidsStr);
                        }
                    }

                    // Statut
                    if (statutStr != null && !statutStr.isEmpty()) {
                        try {
                            colisBuilder.statut(StatutColis.valueOf(statutStr));
                        } catch (IllegalArgumentException e) {
                            colisBuilder.statut(StatutColis.EN_ATTENTE);
                        }
                    } else {
                        colisBuilder.statut(StatutColis.EN_ATTENTE);
                    }

                    Colis colis = colisBuilder.build();
                    colisRepository.save(colis);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Ligne " + (i + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }
        } catch (IOException | CsvException e) {
            log.error("Erreur lors de l'import CSV des colis", e);
            throw new RuntimeException("Erreur lors de l'import CSV", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("errors", errorCount);
        result.put("errorDetails", errors);
        return result;
    }

    // ==================== UTILITAIRES ====================

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
