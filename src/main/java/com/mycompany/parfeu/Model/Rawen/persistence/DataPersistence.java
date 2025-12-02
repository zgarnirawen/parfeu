/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.persistence;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Rawen.exception.DatabaseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Gestionnaire de persistance des données du pare-feu.
 * 
 * Fonctionnalités :
 * - Sauvegarde des décisions en CSV
 * - Chargement des décisions depuis CSV
 * - Sauvegarde de la configuration
 * - Export des statistiques
 * 
 * Concepts Java utilisés :
 * ✅ Try-with-resources (automatique fermeture des flux)
 * ✅ Streams + Lambda pour le traitement des fichiers
 * ✅ Exception personnalisée (DatabaseException)
 * 
 * @author ZGARNI
 */
public final class DataPersistence {
    
    private static final String DATA_DIR = "firewall_data";
    private static final String DECISIONS_FILE = "decisions.csv";
    private static final String CONFIG_FILE = "config.properties";
    private static final String STATS_FILE = "statistics.txt";
    
    private final Path dataDirectory;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructeur - Crée le répertoire de données si nécessaire.
     */
    public DataPersistence() throws DatabaseException {
        this.dataDirectory = Paths.get(DATA_DIR);
        try {
            Files.createDirectories(dataDirectory);
            System.out.println("✓ Répertoire de données initialisé : " + dataDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new DatabaseException("Impossible de créer le répertoire de données", e);
        }
    }

    /**
     * Constructeur avec répertoire personnalisé.
     */
    public DataPersistence(String customDirectory) throws DatabaseException {
        this.dataDirectory = Paths.get(customDirectory);
        try {
            Files.createDirectories(dataDirectory);
            System.out.println("✓ Répertoire de données initialisé : " + dataDirectory.toAbsolutePath());
        } catch (IOException e) {
            throw new DatabaseException("Impossible de créer le répertoire de données", e);
        }
    }

    // ========== SAUVEGARDE DES DÉCISIONS (CSV) ==========

    /**
     * ✅ TRY-WITH-RESOURCES #1
     * Sauvegarde toutes les décisions en CSV.
     * 
     * @param decisions liste des décisions à sauvegarder
     * @throws DatabaseException si l'écriture échoue
     */
    public void saveDecisions(List<DecisionResult> decisions) throws DatabaseException {
        Objects.requireNonNull(decisions, "decisions ne peut pas être null");
        
        Path filePath = dataDirectory.resolve(DECISIONS_FILE);
        
        // ✅ TRY-WITH-RESOURCES : fermeture automatique du BufferedWriter
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(filePath.toFile()), 
                    StandardCharsets.UTF_8
                )
             )) {
            
            // En-tête CSV
            writer.write(DecisionResult.getCSVHeader());
            writer.newLine();
            
            // Écriture des décisions
            for (DecisionResult decision : decisions) {
                writer.write(decision.toCSV());
                writer.newLine();
            }
            
            System.out.println("✓ " + decisions.size() + " décisions sauvegardées dans " + filePath);
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la sauvegarde des décisions", e);
        }
    }

    /**
     * ✅ TRY-WITH-RESOURCES #2
     * Ajoute une décision au fichier CSV existant (mode append).
     * 
     * @param decision décision à ajouter
     * @throws DatabaseException si l'écriture échoue
     */
    public void appendDecision(DecisionResult decision) throws DatabaseException {
        Objects.requireNonNull(decision, "decision ne peut pas être null");
        
        Path filePath = dataDirectory.resolve(DECISIONS_FILE);
        
        // ✅ TRY-WITH-RESOURCES : mode append
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(filePath.toFile(), true), // true = append
                    StandardCharsets.UTF_8
                )
             )) {
            
            writer.write(decision.toCSV());
            writer.newLine();
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de l'ajout de la décision", e);
        }
    }

    // ========== CHARGEMENT DES DÉCISIONS ==========

    /**
     * ✅ TRY-WITH-RESOURCES #3
     * Charge toutes les décisions depuis le CSV.
     * Utilise Stream + Lambda pour le traitement.
     * 
     * @return liste des décisions chargées
     * @throws DatabaseException si la lecture échoue
     */
    public List<String> loadDecisionsRaw() throws DatabaseException {
        Path filePath = dataDirectory.resolve(DECISIONS_FILE);
        
        if (!Files.exists(filePath)) {
            System.out.println("⚠ Aucun fichier de décisions trouvé");
            return new ArrayList<>();
        }
        
        // ✅ TRY-WITH-RESOURCES : fermeture automatique du BufferedReader
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(filePath.toFile()),
                    StandardCharsets.UTF_8
                )
             )) {
            
            // ✅ STREAM + LAMBDA
            return reader.lines()
                    .skip(1) // Ignorer l'en-tête
                    .filter(line -> !line.trim().isEmpty())
                    .toList();
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du chargement des décisions", e);
        }
    }

    /**
     * ✅ TRY-WITH-RESOURCES #4
     * Compte le nombre de décisions dans le fichier.
     * 
     * @return nombre de décisions
     * @throws DatabaseException si la lecture échoue
     */
    public long countDecisions() throws DatabaseException {
        Path filePath = dataDirectory.resolve(DECISIONS_FILE);
        
        if (!Files.exists(filePath)) {
            return 0;
        }
        
        // ✅ TRY-WITH-RESOURCES avec Stream
        try (Stream<String> lines = Files.lines(filePath, StandardCharsets.UTF_8)) {
            return lines.skip(1) // Ignorer l'en-tête
                       .filter(line -> !line.trim().isEmpty())
                       .count();
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du comptage des décisions", e);
        }
    }

    // ========== SAUVEGARDE DE LA CONFIGURATION ==========

    /**
     * ✅ TRY-WITH-RESOURCES #5
     * Sauvegarde la configuration du pare-feu.
     * 
     * @param config configuration à sauvegarder
     * @throws DatabaseException si l'écriture échoue
     */
    public void saveConfig(FirewallConfig config) throws DatabaseException {
        Objects.requireNonNull(config, "config ne peut pas être null");
        
        Path filePath = dataDirectory.resolve(CONFIG_FILE);
        
        // ✅ TRY-WITH-RESOURCES
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(filePath.toFile()),
                    StandardCharsets.UTF_8
                )
             )) {
            
            writer.write("# Configuration du Pare-feu\n");
            writer.write("# Généré le : " + LocalDateTime.now().format(formatter) + "\n\n");
            
            writer.write("blockThreshold=" + config.getBlockThreshold() + "\n");
            writer.write("alertThreshold=" + config.getAlertThreshold() + "\n");
            writer.write("minPacketSize=" + config.getMinPacketSize() + "\n");
            writer.write("maxPacketSize=" + config.getMaxPacketSize() + "\n");
            
            writer.write("\n# Mots suspects\n");
            writer.write("suspiciousWords=" + String.join(",", config.getSuspiciousWords()) + "\n");
            
            writer.write("\n# IPs blacklistées\n");
            writer.write("blacklistedIPs=" + String.join(",", config.getBlacklistedIPs()) + "\n");
            
            writer.write("\n# Ports surveillés\n");
            writer.write("monitoredPorts=" + 
                config.getMonitoredPorts().stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("") + "\n");
            
            System.out.println("✓ Configuration sauvegardée dans " + filePath);
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la sauvegarde de la configuration", e);
        }
    }

    /**
     * ✅ TRY-WITH-RESOURCES #6
     * Charge la configuration depuis le fichier.
     * 
     * @return configuration chargée ou configuration par défaut
     * @throws DatabaseException si la lecture échoue
     */
    public FirewallConfig loadConfig() throws DatabaseException {
        Path filePath = dataDirectory.resolve(CONFIG_FILE);
        
        if (!Files.exists(filePath)) {
            System.out.println("⚠ Aucun fichier de configuration trouvé, utilisation des valeurs par défaut");
            return new FirewallConfig();
        }
        
        FirewallConfig config = new FirewallConfig();
        
        // ✅ TRY-WITH-RESOURCES
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(filePath.toFile()),
                    StandardCharsets.UTF_8
                )
             )) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Ignorer les commentaires et lignes vides
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parser les propriétés
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    
                    switch (key) {
                        case "blockThreshold" -> config.setBlockThreshold(Integer.parseInt(value));
                        case "alertThreshold" -> config.setAlertThreshold(Integer.parseInt(value));
                        case "minPacketSize" -> config.setMinPacketSize(Integer.parseInt(value));
                        case "maxPacketSize" -> config.setMaxPacketSize(Integer.parseInt(value));
                    }
                }
            }
            
            System.out.println("✓ Configuration chargée depuis " + filePath);
            return config;
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du chargement de la configuration", e);
        } catch (NumberFormatException e) {
            throw new DatabaseException("Format de configuration invalide", e);
        }
    }

    // ========== EXPORT DES STATISTIQUES ==========

    /**
     * ✅ TRY-WITH-RESOURCES #7
     * Exporte les statistiques dans un fichier texte.
     * 
     * @param stats contenu des statistiques
     * @throws DatabaseException si l'écriture échoue
     */
    public void exportStatistics(String stats) throws DatabaseException {
        Objects.requireNonNull(stats, "stats ne peut pas être null");
        
        // Nom de fichier avec timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = dataDirectory.resolve("stats_" + timestamp + ".txt");
        
        // ✅ TRY-WITH-RESOURCES
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(filePath.toFile()),
                    StandardCharsets.UTF_8
                )
             )) {
            
            writer.write("========================================\n");
            writer.write("  RAPPORT STATISTIQUES DU PARE-FEU\n");
            writer.write("  Généré le : " + LocalDateTime.now().format(formatter) + "\n");
            writer.write("========================================\n\n");
            
            writer.write(stats);
            
            System.out.println("✓ Statistiques exportées dans " + filePath);
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de l'export des statistiques", e);
        }
    }

    // ========== NETTOYAGE ==========

    /**
     * Supprime tous les fichiers de données.
     * 
     * @throws DatabaseException si la suppression échoue
     */
    public void clearAllData() throws DatabaseException {
        try {
            Files.walk(dataDirectory)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        System.out.println("✓ Fichier supprimé : " + path.getFileName());
                    } catch (IOException e) {
                        System.err.println("⚠ Impossible de supprimer " + path.getFileName());
                    }
                });
            
            System.out.println("✓ Toutes les données ont été effacées");
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du nettoyage des données", e);
        }
    }

    /**
     * Vérifie si le fichier de décisions existe.
     */
    public boolean decisionsFileExists() {
        return Files.exists(dataDirectory.resolve(DECISIONS_FILE));
    }

    /**
     * Retourne le chemin absolu du répertoire de données.
     */
    public String getDataDirectoryPath() {
        return dataDirectory.toAbsolutePath().toString();
    }

    /**
     * Retourne la taille du fichier de décisions en bytes.
     */
    public long getDecisionsFileSize() {
        Path filePath = dataDirectory.resolve(DECISIONS_FILE);
        try {
            return Files.exists(filePath) ? Files.size(filePath) : 0;
        } catch (IOException e) {
            return 0;
        }
    }
}