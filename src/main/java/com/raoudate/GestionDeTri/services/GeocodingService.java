package com.raoudate.GestionDeTri.services;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TrafficModel;
import com.google.maps.model.TravelMode;
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
     * Calcule la distance routière réelle et le temps de trajet entre deux points
     * en utilisant Google Maps Distance Matrix API (EXACTEMENT comme Google Maps web)
     * 
     * @param originLat Latitude d'origine
     * @param originLon Longitude d'origine
     * @param destLat Latitude de destination
     * @param destLon Longitude de destination
     * @return Tableau [distance en km, durée en secondes] ou null si erreur
     */
    private double[] calculerDistanceReelle(double originLat, double originLon, double destLat, double destLon) {
        try {
            LatLng origin = new LatLng(originLat, originLon);
            LatLng destination = new LatLng(destLat, destLon);
            
            // Appel à Distance Matrix API avec options optimisées pour le Togo
            DistanceMatrix matrix = DistanceMatrixApi.newRequest(geoApiContext)
                    .origins(origin)
                    .destinations(destination)
                    .mode(TravelMode.DRIVING)        // 🚗 Mode voiture/moto (routes)
                    .language("fr")                   // 🇫🇷 Langue française
                    .await();
            
            if (matrix.rows != null && matrix.rows.length > 0) {
                DistanceMatrixRow row = matrix.rows[0];
                if (row.elements != null && row.elements.length > 0) {
                    DistanceMatrixElement element = row.elements[0];
                    
                    if (element.status == com.google.maps.model.DistanceMatrixElementStatus.OK) {
                        // Distance en kilomètres (converti de mètres)
                        double distanceKm = element.distance.inMeters / 1000.0;
                        // Durée en secondes
                        long dureeSec = element.duration.inSeconds;
                        
                        return new double[]{distanceKm, dureeSec};
                    }
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Erreur Distance Matrix API, fallback sur Haversine: {}", e.getMessage());
        }
        
        return null; // Retourner null pour indiquer qu'il faut utiliser le fallback
    }
    
    /**
     * Formate la durée en secondes au format "Xh Ymin"
     */
    private String formaterDuree(long secondes) {
        long heures = secondes / 3600;
        long minutes = (secondes % 3600) / 60;
        
        if (heures > 0) {
            return heures + "h " + minutes + "min";
        } else {
            return minutes + "min";
        }
    }

    /**
     * Calcule la distance en km entre deux points GPS (formule de Haversine)
     * FALLBACK si Distance Matrix API échoue
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
     * FALLBACK si Distance Matrix API échoue
     */
    private String estimerTempsTrajet(double distanceKm) {
        // Vitesses réalistes pour livraison de colis au Togo (voiture/moto)
        double vitesseMoyenne;
        if (distanceKm < 5) {
            vitesseMoyenne = 25;  // 25 km/h en ville (trafic dense)
        } else if (distanceKm < 50) {
            vitesseMoyenne = 45;  // 45 km/h routes secondaires
        } else {
            vitesseMoyenne = 65;  // 65 km/h routes nationales
        }
        
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
     * Utilise Distance Matrix API pour distances routières réelles
     */
    public List<AgenceProche> trouverAgencesProches(double latitude, double longitude, int nombre) {
        log.info("🔍 Recherche des {} agences les plus proches de ({}, {}) avec distances routières réelles", 
                nombre, latitude, longitude);

        List<Agences> toutesLesAgences = agenceRepository.findAll();
        
        List<AgenceProche> agencesAvecDistance = new ArrayList<>();

        for (Agences agence : toutesLesAgences) {
            if (agence.getLatitude() != null && agence.getLongitude() != null) {
                
                double distance;
                String tempsEstime;
                
                // Essayer d'abord avec Distance Matrix API (distances routières réelles)
                double[] distanceReelle = calculerDistanceReelle(
                        latitude, longitude,
                        agence.getLatitude(), agence.getLongitude()
                );
                
                if (distanceReelle != null) {
                    // API réussie : utiliser distance et temps réels
                    distance = distanceReelle[0]; // km
                    tempsEstime = formaterDuree((long) distanceReelle[1]); // secondes -> "Xh Ymin"
                } else {
                    // Fallback : Haversine (distance à vol d'oiseau)
                    distance = calculerDistance(
                            latitude, longitude,
                            agence.getLatitude(), agence.getLongitude()
                    );
                    tempsEstime = estimerTempsTrajet(distance);
                }

                AgenceProche ap = AgenceProche.builder()
                        .agenceId(agence.getId())
                        .code(agence.getCode())
                        .nom(agence.getLabel())
                        .region(agence.getRegion())
                        .adresse(agence.getAdresseComplete())
                        .coordonnees(new CoordinatesDTO(agence.getLatitude(), agence.getLongitude()))
                        .distanceKm(Math.round(distance * 100.0) / 100.0) // Arrondir à 2 décimales
                        .tempsEstime(tempsEstime)
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
            log.info("✅ Agence la plus proche: {} - {} ({} km, {})", 
                result.get(0).getNom(), 
                result.get(0).getAdresse(),
                result.get(0).getDistanceKm(),
                result.get(0).getTempsEstime());
        }

        return result;
    }

    /**
     * Trouve les N agences les plus proches d'une coordonnée GPS, 
     * en filtrant d'abord par région si spécifiée
     * 
     * @param latitude Latitude du point de référence
     * @param longitude Longitude du point de référence
     * @param nombre Nombre d'agences à retourner
     * @param regionPrioritaire Région à prioriser (peut être null)
     * @return Liste des agences les plus proches (filtrées par région si spécifiée)
     */
    public List<AgenceProche> trouverAgencesProchesParRegion(
            double latitude, 
            double longitude, 
            int nombre, 
            String regionPrioritaire) {
        
        log.info("🔍 Recherche des {} agences les plus proches de ({}, {}) - Région prioritaire: {}", 
                nombre, latitude, longitude, regionPrioritaire);

        List<Agences> toutesLesAgences = agenceRepository.findAll();
        
        // Si une région est spécifiée, filtrer d'abord par région
        List<Agences> agencesAAnalyser = toutesLesAgences;
        if (regionPrioritaire != null && !regionPrioritaire.trim().isEmpty()) {
            String regionNormalisee = regionPrioritaire.trim().toUpperCase();
            agencesAAnalyser = toutesLesAgences.stream()
                    .filter(a -> a.getRegion() != null && 
                                a.getRegion().trim().toUpperCase().contains(regionNormalisee))
                    .collect(Collectors.toList());
            
            log.info("📊 {} agences trouvées dans la région '{}'", agencesAAnalyser.size(), regionPrioritaire);
        }
        
        // Si aucune agence dans la région spécifiée, fallback sur toutes les agences
        if (agencesAAnalyser.isEmpty() && regionPrioritaire != null) {
            log.warn("⚠️ Aucune agence trouvée dans la région '{}', recherche dans toutes les régions", regionPrioritaire);
            agencesAAnalyser = toutesLesAgences;
        }
        
        List<AgenceProche> agencesAvecDistance = new ArrayList<>();

        for (Agences agence : agencesAAnalyser) {
            if (agence.getLatitude() != null && agence.getLongitude() != null) {
                
                double distance;
                String tempsEstime;
                
                // Essayer d'abord avec Distance Matrix API (distances routières réelles)
                double[] distanceReelle = calculerDistanceReelle(
                        latitude, longitude,
                        agence.getLatitude(), agence.getLongitude()
                );
                
                if (distanceReelle != null) {
                    // API réussie : utiliser distance et temps réels
                    distance = distanceReelle[0]; // km
                    tempsEstime = formaterDuree((long) distanceReelle[1]); // secondes -> "Xh Ymin"
                } else {
                    // Fallback : Haversine (distance à vol d'oiseau)
                    distance = calculerDistance(
                            latitude, longitude,
                            agence.getLatitude(), agence.getLongitude()
                    );
                    tempsEstime = estimerTempsTrajet(distance);
                }

                AgenceProche ap = AgenceProche.builder()
                        .agenceId(agence.getId())
                        .code(agence.getCode())
                        .nom(agence.getLabel())
                        .region(agence.getRegion())
                        .adresse(agence.getAdresseComplete())
                        .coordonnees(new CoordinatesDTO(agence.getLatitude(), agence.getLongitude()))
                        .distanceKm(Math.round(distance * 100.0) / 100.0) // Arrondir à 2 décimales
                        .tempsEstime(tempsEstime)
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
            log.info("✅ Agence la plus proche (région: {}): {} - {} ({} km)", 
                    result.get(0).getRegion(),
                    result.get(0).getNom(), 
                    result.get(0).getAdresse(),
                    result.get(0).getDistanceKm());
        } else {
            log.warn("⚠️ Aucune agence trouvée");
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
