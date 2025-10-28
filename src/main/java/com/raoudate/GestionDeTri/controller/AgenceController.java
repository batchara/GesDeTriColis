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
                    existingAgence.setTel(agenceDTO.getTel() != null ? agenceDTO.getTel() : agenceDTO.getNumTel());
                    existingAgence.setRegion(agenceDTO.getRegion());
                    existingAgence.setAdresseComplete(agenceDTO.getAdresseComplete());
                    existingAgence.setLatitude(agenceDTO.getLatitude());
                    existingAgence.setLongitude(agenceDTO.getLongitude());
                    if (agenceDTO.getStatus() != null) {
                        existingAgence.setStatus(agenceDTO.getStatus());
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
