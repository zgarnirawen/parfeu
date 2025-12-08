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


public final class StorageManager {
    
    private static final String DATA_DIR = "firewall_data";
    private static final String HISTORY_FILE = "historique_blocs.csv";
    private static final String STATS_FILE = "statistiques.txt";
    private static final String CONFIG_FILE = "configuration.properties";
    
    private final Path dataDirectory;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
    
    private void initializeFiles() throws DatabaseException {
        // Initialiser historique
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
        
        // Initialiser configuration
        Path configPath = dataDirectory.resolve(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            try {
                FirewallConfig defaultConfig = new FirewallConfig();
                saveConfiguration(defaultConfig);
                System.out.println("✓ Fichier configuration créé avec valeurs par défaut");
            } catch (Exception e) {
                System.err.println("⚠ Impossible de créer configuration par défaut: " + e.getMessage());
            }
        }
    }

    // ========== GESTION HISTORIQUE BLOCS ==========

    /**
     *  Parse une ligne CSV en gérant les virgules dans les timestamps
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        
        // Ajouter le dernier champ
        result.add(current.toString().trim());
        
        return result.toArray(new String[0]);
    }

    /**
     *  Sauvegarde avec guillemets autour des timestamps et ACTION
     */
    public void saveBlockToHistory(Block block) throws DatabaseException {
        Path historyPath = dataDirectory.resolve(HISTORY_FILE);
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(historyPath.toFile(), true),
                    StandardCharsets.UTF_8
                ))) {
            
            // Format avec guillemets autour du timestamp et action
            String csvLine = String.format("%d,%s,%s,%d,%d,%s,%d,%d,\"%s\",%s,%s,%s",
                block.index(),
                block.srcIP(),
                block.destIP(),
                block.srcPort(),
                block.destPort(),
                block.protocol(),
                block.size(),
                block.timestamp(),
                block.packetTimestamp().toString(),
                block.previousHash(),
                block.hash(),
                block.action()  // ACTION
            );
            
            writer.write(csvLine);
            writer.newLine();
            
            System.out.println("✓ Bloc #" + block.index() + " sauvegardé | Action: " + block.action());
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la sauvegarde du bloc", e);
        }
    }

    /**
     Retourne des objets structurés avec ACTION
     */
    public List<BlockData> loadBlockHistory() throws DatabaseException {
        Path historyPath = dataDirectory.resolve(HISTORY_FILE);
        
        if (!Files.exists(historyPath)) {
            System.out.println("⚠ Aucun historique de blocs trouvé");
            return new ArrayList<>();
        }
        
        List<BlockData> blocks = new ArrayList<>();
        
        try (BufferedReader reader = Files.newBufferedReader(historyPath, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] parts = parseCSVLine(line);
                    
                    // COMPATIBILITÉ : Ancien format (11 colonnes) vs nouveau (12 colonnes)
                    if (parts.length < 11) {
                        System.err.println("⚠️ Ligne invalide (colonnes: " + parts.length + "): " + line);
                        continue;
                    }
                    
                    String action = "LOG";  // Valeur par défaut
                    
                    if (parts.length >= 12) {
                        // Nouveau format avec action
                        action = parts[11];
                    } else {
                        // Ancien format, utiliser LOG par défaut
                        System.out.println("  ⚠️ Ancien format détecté pour bloc #" + parts[0] + ", action=LOG par défaut");
                    }
                    
                    BlockData blockData = new BlockData(
                        Integer.parseInt(parts[0]),      // index
                        parts[1],                         // srcIP
                        parts[2],                         // destIP
                        Integer.parseInt(parts[3]),      // srcPort
                        Integer.parseInt(parts[4]),      // destPort
                        parts[5],                         // protocol
                        Integer.parseInt(parts[6]),      // size
                        Long.parseLong(parts[7]),        // timestamp
                        parts[8],                         // packetTimestamp
                        parts[9],                         // previousHash
                        parts[10],                        // hash
                        action                            // 🔥 action
                    );
                    
                    blocks.add(blockData);
                    
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur parsing ligne: " + e.getMessage());
                    System.err.println("   Ligne: " + line);
                }
            }
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du chargement de l'historique", e);
        }
        
        return blocks;
    }

    public long countBlocks() throws DatabaseException {
        return loadBlockHistory().size();
    }

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

    // ========== CLASSE INTERNE POUR DONNÉES DE BLOC ==========
    
    /**
      Objet structuré pour les données de bloc avec ACTION
     */
    public static class BlockData {
        public final int index;
        public final String srcIP;
        public final String destIP;
        public final int srcPort;
        public final int destPort;
        public final String protocol;
        public final int size;
        public final long timestamp;
        public final String packetTimestamp;
        public final String previousHash;
        public final String hash;
        public final String action;  //  ACTION
        
        public BlockData(int index, String srcIP, String destIP, int srcPort, 
                        int destPort, String protocol, int size, long timestamp,
                        String packetTimestamp, String previousHash, String hash,
                        String action) {
            this.index = index;
            this.srcIP = srcIP;
            this.destIP = destIP;
            this.srcPort = srcPort;
            this.destPort = destPort;
            this.protocol = protocol;
            this.size = size;
            this.timestamp = timestamp;
            this.packetTimestamp = packetTimestamp;
            this.previousHash = previousHash;
            this.hash = hash;
            this.action = action;
        }
    }

    // ========== GESTION STATISTIQUES ==========

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
            
            writer.write("📊 STATISTIQUES GÉNÉRALES\n");
            writer.write("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            writer.write(String.format("Total paquets traités : %d\n", stats.getTotalPackets()));
            writer.write(String.format("  ✓ Acceptés          : %d\n", stats.getAcceptedPackets()));
            writer.write(String.format("  ✗ Bloqués           : %d\n", stats.getDroppedPackets()));
            writer.write(String.format("  ⚠ Alertes           : %d\n", stats.getAlertedPackets()));
            writer.write(String.format("  📝 Journalisés      : %d\n\n", stats.getLoggedPackets()));
            
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
            
            System.out.println("✓ Configuration sauvegardée");
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors de la sauvegarde de la configuration", e);
        }
    }

    public FirewallConfig loadConfiguration() throws DatabaseException {
        Path configPath = dataDirectory.resolve(CONFIG_FILE);
        
        if (!Files.exists(configPath)) {
            System.out.println("⚠ Aucune configuration trouvée");
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
                    
                    try {
                        switch (key) {
                            case "blockThreshold":
                                config.setBlockThreshold(Integer.parseInt(value));
                                break;
                            case "alertThreshold":
                                config.setAlertThreshold(Integer.parseInt(value));
                                break;
                            case "minPacketSize":
                                config.setMinPacketSize(Integer.parseInt(value));
                                break;
                            case "maxPacketSize":
                                config.setMaxPacketSize(Integer.parseInt(value));
                                break;
                            case "suspiciousWords":
                                if (!value.isEmpty()) {
                                    for (String word : value.split(",")) {
                                        config.addSuspiciousWord(word.trim());
                                    }
                                }
                                break;
                            case "blacklistedIPs":
                                if (!value.isEmpty()) {
                                    for (String ip : value.split(",")) {
                                        config.addBlacklistedIP(ip.trim());
                                    }
                                }
                                break;
                            case "monitoredPorts":
                                if (!value.isEmpty()) {
                                    for (String port : value.split(",")) {
                                        config.addMonitoredPort(Integer.parseInt(port.trim()));
                                    }
                                }
                                break;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("⚠ Valeur invalide pour " + key);
                    }
                }
            }
            
            System.out.println("✓ Configuration chargée");
            return config;
            
        } catch (IOException e) {
            throw new DatabaseException("Erreur lors du chargement de la configuration", e);
        }
    }

    // ========== UTILITAIRES ==========

    public String getDataDirectoryPath() {
        return dataDirectory.toAbsolutePath().toString();
    }

    public boolean historyExists() {
        return Files.exists(dataDirectory.resolve(HISTORY_FILE));
    }

    public boolean statsExists() {
        return Files.exists(dataDirectory.resolve(STATS_FILE));
    }

    public boolean configExists() {
        return Files.exists(dataDirectory.resolve(CONFIG_FILE));
    }

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