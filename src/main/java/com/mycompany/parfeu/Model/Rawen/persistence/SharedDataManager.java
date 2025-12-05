package com.mycompany.parfeu.Model.Rawen.persistence;

import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.generator.PaquetSimple;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockChain;
import com.mycompany.parfeu.Model.Rawen.statistics.StatisticsManager;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Rawen.decision.Actions;
import com.mycompany.parfeu.Model.Rawen.exception.DatabaseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 🔥 VERSION FINALE CORRIGÉE - Restauration complète des données
 */
public final class SharedDataManager {
    
    private static SharedDataManager instance;
    private final StatisticsManager statistics;
    private final StorageManager storage;
    private BlockChain blockchain;
    private FirewallConfig configuration;
    
    private boolean isReconstructing = false;
    
    private SharedDataManager() {
        // 🔥 INITIALISATION IMMÉDIATE DES CHAMPS FINAL
        this.statistics = new StatisticsManager();
        
        StorageManager tempStorage = null;
        try {
            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.println("║        INITIALISATION SHARED DATA MANAGER               ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝\n");
            
            tempStorage = new StorageManager();
            this.blockchain = new BlockChain();
            
            // 🔥 CHARGEMENT IMMÉDIAT DE TOUTES LES DONNÉES
            loadAllData(tempStorage);
            
            System.out.println("✅ SharedDataManager prêt\n");
            printSummary();
            
        } catch (DatabaseException e) {
            System.err.println("⚠️  Erreur init: " + e.getMessage());
            e.printStackTrace();
            // Créer un StorageManager par défaut si l'initialisation échoue
            try {
                tempStorage = new StorageManager();
            } catch (DatabaseException ex) {
                System.err.println("⚠️  Impossible de créer StorageManager: " + ex.getMessage());
            }
        } finally {
            // Assigner le storage (jamais null grâce au try-catch)
            this.storage = tempStorage != null ? tempStorage : createDummyStorage();
        }
    }
    
    /**
     * Crée un StorageManager factice en cas d'échec total
     */
    private StorageManager createDummyStorage() {
        try {
            return new StorageManager();
        } catch (DatabaseException e) {
            throw new RuntimeException("Impossible d'initialiser le système de stockage", e);
        }
    }
    
    public static synchronized SharedDataManager getInstance() {
        if (instance == null) {
            instance = new SharedDataManager();
        }
        return instance;
    }
    
    /**
     * 🔥 CHARGEMENT COMPLET - IMMÉDIAT AU DÉMARRAGE
     */
    private void loadAllData(StorageManager storage) {
        System.out.println("🔄 Chargement des données persistantes...\n");
        
        try {
            isReconstructing = true;
            
            // 1️⃣ CONFIGURATION (TOUJOURS EN PREMIER)
            System.out.println("📋 1. Configuration...");
            try {
                configuration = storage.loadConfiguration();
                if (configuration != null) {
                    System.out.println("  ✅ Configuration chargée depuis fichier");
                    System.out.println("     - Seuil blocage: " + configuration.getBlockThreshold());
                    System.out.println("     - Seuil alerte: " + configuration.getAlertThreshold());
                    System.out.println("     - Mots suspects: " + configuration.getSuspiciousWords().size());
                    System.out.println("     - IPs blacklistées: " + configuration.getBlacklistedIPs().size());
                    System.out.println("     - Ports surveillés: " + configuration.getMonitoredPorts().size());
                } else {
                    configuration = new FirewallConfig();
                    System.out.println("  ℹ️  Configuration par défaut créée");
                }
            } catch (DatabaseException e) {
                configuration = new FirewallConfig();
                System.out.println("  ⚠️  Utilisation configuration par défaut");
            }
            
            // 2️⃣ BLOCKCHAIN (RECONSTRUCTION DEPUIS CSV)
            System.out.println("\n🔗 2. Blockchain...");
            reconstructBlockchainFromCSV(storage);
            
            // 3️⃣ STATISTIQUES (DÉJÀ PEUPLÉES VIA RECONSTRUCTION)
            System.out.println("\n📊 3. Statistiques finales...");
            System.out.println("  ✅ Total paquets: " + statistics.getTotalPackets());
            System.out.println("  ✅ Acceptés: " + statistics.getAcceptedPackets());
            System.out.println("  ✅ Bloqués: " + statistics.getDroppedPackets());
            
            isReconstructing = false;
            System.out.println("\n✅ Chargement terminé avec succès!");
            
        } catch (Exception e) {
            isReconstructing = false;
            System.err.println("⚠️  Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 🔥 RECONSTRUCTION BLOCKCHAIN DEPUIS CSV - VERSION CORRIGÉE
     */
    private void reconstructBlockchainFromCSV(StorageManager storage) {
        try {
            List<String> lines = storage.loadBlockHistory();
            
            if (lines.isEmpty()) {
                System.out.println("  ℹ️  Blockchain vide (genesis uniquement)");
                return;
            }
            
            System.out.println("  📂 " + lines.size() + " lignes à traiter");
            
            int reconstructed = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("Index,")) {
                    continue;
                }
                
                try {
                    String[] parts = line.split(",");
                    
                    if (parts.length < 11) {
                        System.err.println("  ⚠️  Ligne invalide: " + parts.length + " colonnes");
                        continue;
                    }
                    
                    int index = Integer.parseInt(parts[0].trim());
                    String srcIP = parts[1].trim();
                    
                    // Skip Genesis (déjà créé)
                    if (index == 0 && "0.0.0.0".equals(srcIP)) {
                        System.out.println("  ⏭️  Genesis bloc skippé");
                        continue;
                    }
                    
                    // Extraire toutes les données
                    String destIP = parts[2].trim();
                    int srcPort = Integer.parseInt(parts[3].trim());
                    int destPort = Integer.parseInt(parts[4].trim());
                    String protocol = parts[5].trim();
                    int size = Integer.parseInt(parts[6].trim());
                    long blockTimestamp = Long.parseLong(parts[7].trim());
                    
                    LocalDateTime packetTimestamp;
                    try {
                        packetTimestamp = LocalDateTime.parse(parts[8].trim(), formatter);
                    } catch (Exception e) {
                        packetTimestamp = LocalDateTime.now();
                    }
                    
                    String previousHash = parts[9].trim();
                    String hash = parts[10].trim();
                    
                    // Créer un paquet pour la décision
                    Packet packet = new PaquetSimple(
                        srcIP, destIP, srcPort, destPort,
                        protocol, "Restored from history", 
                        packetTimestamp
                    );
                    
                    // Créer une décision fictive pour les stats
                    DecisionResult decision = new DecisionResult(
                        packet,
                        new ArrayList<>(),
                        0,
                        Actions.LOG,
                        "Restored from history"
                    );
                    
                    // 🔥 CRÉER LE BLOC AVEC LE HASH ORIGINAL
                    Block restoredBlock = new Block(
                        index,
                        List.of(decision),
                        previousHash,
                        blockTimestamp,
                        hash,
                        srcIP, destIP, srcPort, destPort,
                        protocol, "Restored", size,
                        packetTimestamp,
                        true  // fromCSV flag
                    );
                    
                    // Ajouter directement à la blockchain
                    blockchain.getChain().add(restoredBlock);
                    
                    // Enregistrer dans les stats
                    statistics.recordDecision(decision);
                    
                    reconstructed++;
                    
                    if (reconstructed <= 3) {
                        System.out.println("  ✓ Bloc #" + index + " : " + 
                                         srcIP + " -> " + destIP + " (" + protocol + ")");
                    }
                    
                } catch (Exception e) {
                    System.err.println("  ⚠️  Erreur ligne: " + e.getMessage());
                }
            }
            
            System.out.println("  ✅ " + reconstructed + " blocs reconstruits");
            System.out.println("  📊 Blockchain totale: " + blockchain.getSize() + " blocs");
            
        } catch (DatabaseException e) {
            System.out.println("  ℹ️  Pas d'historique trouvé: " + e.getMessage());
        }
    }
    
    /**
     * 🔥 AJOUT D'UNE NOUVELLE DÉCISION
     */
    public void addDecision(DecisionResult decision) {
        if (isReconstructing) {
            blockchain.addDecision(decision);
            statistics.recordDecision(decision);
            return;
        }
        
        try {
            System.out.println("\n💾 Nouvelle décision...");
            
            blockchain.addDecision(decision);
            statistics.recordDecision(decision);
            
            // 🔥 SAUVEGARDER IMMÉDIATEMENT
            saveAllData();
            
            System.out.println("✅ Décision sauvegardée\n");
            
        } catch (Exception e) {
            System.err.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 🔥 SAUVEGARDE COMPLÈTE
     */
    private void saveAllData() throws DatabaseException {
        // Sauvegarder TOUS les blocs
        List<Block> chain = blockchain.getChain();
        if (!chain.isEmpty()) {
            storage.clearHistory();
            for (Block block : chain) {
                storage.saveBlockToHistory(block);
            }
        }
        
        // Sauvegarder les statistiques
        storage.saveStatistics(statistics);
        
        // Sauvegarder la configuration
        if (configuration != null) {
            storage.saveConfiguration(configuration);
        }
    }
    
    /**
     * 🔥 SAUVEGARDE CONFIGURATION EXPLICITE
     */
    public void saveConfiguration(FirewallConfig config) {
        try {
            this.configuration = config;
            storage.saveConfiguration(config);
            System.out.println("✓ Configuration sauvegardée");
        } catch (DatabaseException e) {
            System.err.println("✗ Erreur config: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters
    public BlockChain getBlockchain() { 
        return blockchain; 
    }
    
    public StatisticsManager getStatistics() { 
        return statistics; 
    }
    
    public FirewallConfig getConfiguration() { 
        return configuration != null ? configuration : new FirewallConfig(); 
    }
    
    /**
     * Reset complet
     */
    public void reset() {
        try {
            statistics.reset();
            blockchain = new BlockChain();
            storage.clearAll();
            configuration = new FirewallConfig();
            System.out.println("✓ Reset complet");
        } catch (DatabaseException e) {
            System.err.println("✗ Erreur reset: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Résumé console
     */
    public void printSummary() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                 ÉTAT DU SYSTÈME                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("🔗 Blockchain    : " + blockchain.getSize() + " blocs");
        System.out.println("📊 Paquets       : " + statistics.getTotalPackets());
        System.out.println("   ✓ Acceptés    : " + statistics.getAcceptedPackets());
        System.out.println("   ✗ Bloqués     : " + statistics.getDroppedPackets());
        System.out.println("⚙️  Configuration :");
        System.out.println("   - Seuil blocage: " + configuration.getBlockThreshold());
        System.out.println("   - Seuil alerte : " + configuration.getAlertThreshold());
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }
}