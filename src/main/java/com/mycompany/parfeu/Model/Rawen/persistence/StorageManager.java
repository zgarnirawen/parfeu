package com.mycompany.parfeu.Model.Rawen.persistence;

import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.exception.DatabaseException;
import com.mycompany.parfeu.Model.Rawen.statistics.StatisticsManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Gestionnaire centralisé pour les 3 fichiers de stockage :
 * 1. historique_blocs.csv - Historique complet de tous les blocs
 * 2. statistiques.txt - Statistiques du pare-feu
 * 3. configuration.properties - Configuration du pare-feu
 * 
 * @author ZGARNI
 */
public final class StorageManager {
    
    private static final String DATA_DIR = "firewall_data";
    private static final String HISTORY_FILE = "historique_blocs.csv";
    private static final String STATS_FILE = "statistiques.txt";
    private static final String CONFIG_FILE = "configuration.properties";
    
    private final Path dataDirectory;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructeur - Crée le répertoire de données si nécessaire.
     */
    public StorageManager() throws DatabaseException {
        this.dataDirectory = Paths.get(DATA_DIR);
        try {
            Files.createDirectories(dataDirectory);
            System.out.println("✓ Répertoire de stockage initialisé : " + dataDirectory.toAbsolutePath());
            initializeFiles();
        } catch (IOException e) {
            throw new DatabaseException("Impossible de créer le répertoire de données", e);
        }
    }
    
    /**
     * Initialise les fichiers s'ils n'existent pas.
     */
    private void initializeFiles() throws DatabaseException {
        Path historyPath = dataDirectory.resolve(HISTORY_FILE);
        if (!Files.exists(historyPath)) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(historyPath.toFile()),
                        StandardCharsets.UTF_8
                    ))) {
                writer.write(Block.getCSVHeader());
                writer.newLine();
                System.out.println("✓ Fichier historique créé");
            } catch (IOException e) {
                throw new DatabaseException("Erreur création fichier historique", e);
            }
        }
    }

    // ========== GESTION HISTORIQUE BLOCS ==========

    /**
     * Sauvegarde un bloc dans l'historique (mode append).
     */
    public void saveBlockToHistory(Block block) throws DatabaseException {
        Path historyPath = dataDirectory.resolve(HISTORY_FILE);
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(historyPath.toFile(), true), // append
                    StandardCharsets.UTF_8
                ))) {
            
            writer.write(block.toCSV());
            writer.newLine();
            
            System.out.println("✓ Bloc #" + block.index() + " sauvegardé dans l'historique");
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la sauvegarde du bloc", e);
        }
    }

    /**
     * Charge tous les blocs depuis l'historique.
     */
    public List<String> loadBlockHistory() throws DatabaseException {
        Path historyPath = dataDirectory.resolve(HISTORY_FILE);
        
        if (!Files.exists(historyPath)) {
            System.out.println("⚠ Aucun historique de blocs trouvé");
            return new ArrayList<>();
        }
        
        try (Stream<String> lines = Files.lines(historyPath, StandardCharsets.UTF_8)) {
            return lines.skip(1) // Ignorer l'en-tête
                       .filter(line -> !line.trim().isEmpty())
                       .toList();
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du chargement de l'historique", e);
        }
    }

    /**
     * Compte le nombre de blocs dans l'historique.
     */
    public long countBlocks() throws DatabaseException {
        Path historyPath = dataDirectory.resolve(HISTORY_FILE);
        
        if (!Files.exists(historyPath)) {
            return 0;
        }
        
        try (Stream<String> lines = Files.lines(historyPath, StandardCharsets.UTF_8)) {
            return lines.skip(1)
                       .filter(line -> !line.trim().isEmpty())
                       .count();
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du comptage des blocs", e);
        }
    }

    /**
     * Efface tout l'historique (garde uniquement l'en-tête).
     */
    public void clearHistory() throws DatabaseException {
        Path historyPath = dataDirectory.resolve(HISTORY_FILE);
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(historyPath.toFile()),
                    StandardCharsets.UTF_8
                ))) {
            
            writer.write(Block.getCSVHeader());
            writer.newLine();
            
            System.out.println("✓ Historique effacé");
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de l'effacement de l'historique", e);
        }
    }

    // ========== GESTION STATISTIQUES ==========

    /**
     * Sauvegarde les statistiques complètes.
     */
    public void saveStatistics(StatisticsManager stats) throws DatabaseException {
        Path statsPath = dataDirectory.resolve(STATS_FILE);
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(statsPath.toFile()),
                    StandardCharsets.UTF_8
                ))) {
            
            writer.write("╔══════════════════════════════════════════════════════════════╗\n");
            writer.write("║         STATISTIQUES DU PARE-FEU                            ║\n");
            writer.write("║         Généré le : " + LocalDateTime.now().format(formatter) + "                   ║\n");
            writer.write("╚══════════════════════════════════════════════════════════════╝\n\n");
            
            // Statistiques générales
            writer.write("📊 STATISTIQUES GÉNÉRALES\n");
            writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            writer.write(String.format("Total paquets traités : %d\n", stats.getTotalPackets()));
            writer.write(String.format("  ✓ Acceptés          : %d\n", stats.getAcceptedPackets()));
            writer.write(String.format("  ✗ Bloqués           : %d\n", stats.getDroppedPackets()));
            writer.write(String.format("  ⚠ Alertes           : %d\n", stats.getAlertedPackets()));
            writer.write(String.format("  📝 Journalisés      : %d\n\n", stats.getLoggedPackets()));
            
            // Taux
            if (stats.getTotalPackets() > 0) {
                double acceptRate = (stats.getAcceptedPackets() * 100.0) / stats.getTotalPackets();
                double blockRate = (stats.getDroppedPackets() * 100.0) / stats.getTotalPackets();
                double alertRate = (stats.getAlertedPackets() * 100.0) / stats.getTotalPackets();
                
                writer.write("📈 TAUX\n");
                writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                writer.write(String.format("  Taux d'acceptation : %.2f%%\n", acceptRate));
                writer.write(String.format("  Taux de blocage    : %.2f%%\n", blockRate));
                writer.write(String.format("  Taux d'alerte      : %.2f%%\n\n", alertRate));
            }
            
            // Statistiques par IP (top 5)
            var ipStats = stats.getIPStatistics();
            if (ipStats != null && !ipStats.isEmpty()) {
                writer.write("🌐 TOP 5 IP SOURCES\n");
                writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                
                ipStats.values().stream()
                    .sorted((a, b) -> Integer.compare(b.totalPackets, a.totalPackets))
                    .limit(5)
                    .forEach(stat -> {
                        double ipBlockRate = stat.totalPackets > 0 
                            ? (stat.blockedPackets * 100.0) / stat.totalPackets 
                            : 0;
                        try {
                            writer.write(String.format("  %s : %d paquets (%.1f%% bloqués)\n",
                                stat.ipAddress, stat.totalPackets, ipBlockRate));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            }
            
            writer.write("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            System.out.println("✓ Statistiques sauvegardées dans " + statsPath);
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la sauvegarde des statistiques", e);
        }
    }

    /**
     * Charge les statistiques depuis le fichier.
     */
    public String loadStatistics() throws DatabaseException {
        Path statsPath = dataDirectory.resolve(STATS_FILE);
        
        if (!Files.exists(statsPath)) {
            return "Aucune statistique disponible";
        }
        
        try {
            return Files.readString(statsPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du chargement des statistiques", e);
        }
    }

    // ========== GESTION CONFIGURATION ==========

    /**
     * Sauvegarde la configuration du pare-feu.
     */
    public void saveConfiguration(FirewallConfig config) throws DatabaseException {
        Path configPath = dataDirectory.resolve(CONFIG_FILE);
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(configPath.toFile()),
                    StandardCharsets.UTF_8
                ))) {
            
            writer.write("# Configuration du Pare-feu\n");
            writer.write("# Généré le : " + LocalDateTime.now().format(formatter) + "\n\n");
            
            writer.write("# Seuils de décision\n");
            writer.write("blockThreshold=" + config.getBlockThreshold() + "\n");
            writer.write("alertThreshold=" + config.getAlertThreshold() + "\n\n");
            
            writer.write("# Limites de taille\n");
            writer.write("minPacketSize=" + config.getMinPacketSize() + "\n");
            writer.write("maxPacketSize=" + config.getMaxPacketSize() + "\n\n");
            
            writer.write("# Mots suspects\n");
            writer.write("suspiciousWords=" + String.join(",", config.getSuspiciousWords()) + "\n\n");
            
            writer.write("# IPs blacklistées\n");
            writer.write("blacklistedIPs=" + String.join(",", config.getBlacklistedIPs()) + "\n\n");
            
            writer.write("# Ports surveillés\n");
            writer.write("monitoredPorts=" + 
                config.getMonitoredPorts().stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("") + "\n");
            
            System.out.println("✓ Configuration sauvegardée dans " + configPath);
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la sauvegarde de la configuration", e);
        }
    }

    /**
     * Charge la configuration depuis le fichier.
     */
    public FirewallConfig loadConfiguration() throws DatabaseException {
        Path configPath = dataDirectory.resolve(CONFIG_FILE);
        
        if (!Files.exists(configPath)) {
            System.out.println("⚠ Aucune configuration trouvée, utilisation des valeurs par défaut");
            return new FirewallConfig();
        }
        
        FirewallConfig config = new FirewallConfig();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(configPath.toFile()),
                    StandardCharsets.UTF_8
                ))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
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
            
            System.out.println("✓ Configuration chargée depuis " + configPath);
            return config;
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du chargement de la configuration", e);
        }
    }

    // ========== UTILITAIRES ==========

    /**
     * Retourne le chemin absolu du répertoire de données.
     */
    public String getDataDirectoryPath() {
        return dataDirectory.toAbsolutePath().toString();
    }

    /**
     * Vérifie si les fichiers existent.
     */
    public boolean historyExists() {
        return Files.exists(dataDirectory.resolve(HISTORY_FILE));
    }

    public boolean statsExists() {
        return Files.exists(dataDirectory.resolve(STATS_FILE));
    }

    public boolean configExists() {
        return Files.exists(dataDirectory.resolve(CONFIG_FILE));
    }

    /**
     * Retourne la taille des fichiers en bytes.
     */
    public long getHistoryFileSize() {
        try {
            Path path = dataDirectory.resolve(HISTORY_FILE);
            return Files.exists(path) ? Files.size(path) : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Efface tous les fichiers.
     */
    public void clearAll() throws DatabaseException {
        try {
            clearHistory();
            
            Path statsPath = dataDirectory.resolve(STATS_FILE);
            if (Files.exists(statsPath)) {
                Files.delete(statsPath);
            }
            
            Path configPath = dataDirectory.resolve(CONFIG_FILE);
            if (Files.exists(configPath)) {
                Files.delete(configPath);
            }
            
            System.out.println("✓ Tous les fichiers effacés");
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de l'effacement des fichiers", e);
        }
    }
}