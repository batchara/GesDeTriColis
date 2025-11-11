package com.raoudate.GestionDeTri.services;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.raoudate.GestionDeTri.Dto.AgenceProche;
import com.raoudate.GestionDeTri.Dto.CoordinatesDTO;
import com.raoudate.GestionDeTri.Dto.GeocodingResultDTO;
import com.raoudate.GestionDeTri.model.Agences;
import com.raoudate.GestionDeTri.repository.AgenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de géocodage utilisant Google Maps Geocoding API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeocodingService {

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Value("${google.maps.region:TG}")
    private String region;

    @Value("${google.maps.language:fr}")
    private String language;

    private final AgenceRepository agenceRepository;
    private GeoApiContext geoApiContext;

    @PostConstruct
    public void init() {
        log.info("🗺️ Initialisation du service de géocodage Google Maps");
        log.info("📍 Région: {}, Langue: {}", region, language);
        
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    @PreDestroy
    public void cleanup() {
        if (geoApiContext != null) {
            geoApiContext.shutdown();
            log.info("🔒 Arrêt du service de géocodage");
        }
    }

    /**
     * Géocode une adresse en coordonnées GPS
     */
    public GeocodingResultDTO geocodeAdresse(String adresse) {
        try {
            log.info("🔍 Géocodage de l'adresse: {}", adresse);

            // Ajouter le pays si non présent
            String adresseComplete = adresse;
            if (!adresse.toLowerCase().contains("togo") && !adresse.toLowerCase().contains("tog")) {
                adresseComplete = adresse + ", Togo";
            }

            GeocodingResult[] results = GeocodingApi.newRequest(geoApiContext)
                    .address(adresseComplete)
                    .region(region)
                    .language(language)
                    .await();

            if (results != null && results.length > 0) {
                GeocodingResult result = results[0];
                LatLng location = result.geometry.location;

                CoordinatesDTO coords = new CoordinatesDTO(location.lat, location.lng);

                log.info("✅ Géocodage réussi: {} → ({}, {})", 
                    result.formattedAddress, location.lat, location.lng);

                return GeocodingResultDTO.builder()
                        .adresseOriginale(adresse)
                        .adresseFormattee(result.formattedAddress)
                        .coordonnees(coords)
                        .succes(true)
                        .build();
            } else {
                log.warn("⚠️ Aucun résultat pour l'adresse: {}", adresse);
                return GeocodingResultDTO.builder()
                        .adresseOriginale(adresse)
                        .succes(false)
                        .messageErreur("Adresse non trouvée")
                        .build();
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors du géocodage de l'adresse: {}", adresse, e);
            return GeocodingResultDTO.builder()
                    .adresseOriginale(adresse)
                    .succes(false)
                    .messageErreur("Erreur de géocodage: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Calcule la distance en km entre deux points GPS (formule de Haversine)
     */
    public double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int RAYON_TERRE_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RAYON_TERRE_KM * c;
    }

    /**
     * Estime le temps de trajet en fonction de la distance
     * Vitesse moyenne estimée: 40 km/h en ville, 60 km/h hors ville
     */
    private String estimerTempsTrajet(double distanceKm) {
        // Vitesse moyenne en km/h (conservatrice pour le Togo)
        double vitesseMoyenne = distanceKm < 10 ? 30 : 50;
        double heures = distanceKm / vitesseMoyenne;
        
        int heuresEntier = (int) heures;
        int minutes = (int) ((heures - heuresEntier) * 60);

        if (heuresEntier > 0) {
            return heuresEntier + "h " + minutes + "min";
        } else {
            return minutes + "min";
        }
    }

    /**
     * Trouve l'agence la plus proche d'une coordonnée GPS
     */
    public AgenceProche trouverAgenceLaPlusProche(double latitude, double longitude) {
        List<AgenceProche> agencesProches = trouverAgencesProches(latitude, longitude, 1);
        return agencesProches.isEmpty() ? null : agencesProches.get(0);
    }

    /**
     * Trouve les N agences les plus proches d'une coordonnée GPS
     */
    public List<AgenceProche> trouverAgencesProches(double latitude, double longitude, int nombre) {
        log.info("🔍 Recherche des {} agences les plus proches de ({}, {})", nombre, latitude, longitude);

        List<Agences> toutesLesAgences = agenceRepository.findAll();
        
        List<AgenceProche> agencesAvecDistance = new ArrayList<>();

        for (Agences agence : toutesLesAgences) {
            if (agence.getLatitude() != null && agence.getLongitude() != null) {
                double distance = calculerDistance(
                        latitude, longitude,
                        agence.getLatitude(), agence.getLongitude()
                );

                AgenceProche ap = AgenceProche.builder()
                        .agenceId(agence.getId())
                        .code(agence.getCode())
                        .nom(agence.getLabel())
                        .region(agence.getRegion())
                        .adresse(agence.getAdresseComplete())
                        .coordonnees(new CoordinatesDTO(agence.getLatitude(), agence.getLongitude()))
                        .distanceKm(Math.round(distance * 100.0) / 100.0) // Arrondir à 2 décimales
                        .tempsEstime(estimerTempsTrajet(distance))
                        .build();

                agencesAvecDistance.add(ap);
            }
        }

        // Trier par distance et prendre les N premières
        List<AgenceProche> result = agencesAvecDistance.stream()
                .sorted(Comparator.comparingDouble(AgenceProche::getDistanceKm))
                .limit(nombre)
                .collect(Collectors.toList());

        if (!result.isEmpty()) {
            log.info("✅ Agence la plus proche: {} ({} km)", 
                result.get(0).getNom(), result.get(0).getDistanceKm());
        }

        return result;
    }

    /**
     * Géocode une adresse et trouve l'agence la plus proche
     */
    public AgenceProche geocoderEtTrouverAgence(String adresse) {
        GeocodingResultDTO geocoding = geocodeAdresse(adresse);
        
        if (geocoding.getSucces() && geocoding.getCoordonnees() != null) {
            return trouverAgenceLaPlusProche(
                    geocoding.getCoordonnees().getLatitude(),
                    geocoding.getCoordonnees().getLongitude()
            );
        }
        
        return null;
    }
}
