package com.raoudate.GestionDeTri.controller;

import com.raoudate.GestionDeTri.Dto.AgenceDTO;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agences")
@RequiredArgsConstructor
public class AgenceController {

    private final AgenceRepository agenceRepository;

    @GetMapping
    public ResponseEntity<List<AgenceDTO>> getAllAgences() {
        System.out.println("********** GETTING ALL AGENCIES **********");
        List<Agences> agences = agenceRepository.findAll();
        List<AgenceDTO> agenceDTOs = agences.stream()
                .map(AgenceDTO::fromEntity)
                .collect(Collectors.toList());
        System.out.println("********** NOMBRE D'AGENCES: " + agenceDTOs.size() + " **********");
        return ResponseEntity.ok(agenceDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgenceDTO> getAgenceById(@PathVariable Integer id) {
        return agenceRepository.findById(id)
                .map(AgenceDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AgenceDTO> createAgence(@RequestBody AgenceDTO agenceDTO) {
        System.out.println("********** CREATING AGENCY: " + agenceDTO.getNom() + " **********");
        Agences agence = AgenceDTO.toEntity(agenceDTO);
        Agences savedAgence = agenceRepository.save(agence);
        return ResponseEntity.ok(AgenceDTO.fromEntity(savedAgence));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgenceDTO> updateAgence(
            @PathVariable Integer id,
            @RequestBody AgenceDTO agenceDTO) {
        return agenceRepository.findById(id)
                .map(existingAgence -> {
                    existingAgence.setLabel(agenceDTO.getNom());
                    existingAgence.setCode(agenceDTO.getCode());
                    existingAgence.setEmail(agenceDTO.getEmail());
                    existingAgence.setTel(agenceDTO.getTel());
                    existingAgence.setRegion(agenceDTO.getRegion());
                    if (agenceDTO.getAdresse() != null && existingAgence.getAdresse() != null) {
                        existingAgence.getAdresse().setAdresseComplete(agenceDTO.getAdresse().getAdresseComplete());
                        existingAgence.getAdresse().setRue(agenceDTO.getAdresse().getRue());
                        existingAgence.getAdresse().setTypeAdresse(agenceDTO.getAdresse().getTypeAdresse());
                        existingAgence.getAdresse().setLatitude(agenceDTO.getAdresse().getLatitude());
                        existingAgence.getAdresse().setLongitude(agenceDTO.getAdresse().getLongitude());
                    }
                    Agences updatedAgence = agenceRepository.save(existingAgence);
                    return ResponseEntity.ok(AgenceDTO.fromEntity(updatedAgence));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgence(@PathVariable Integer id) {
        if (agenceRepository.existsById(id)) {
            agenceRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
