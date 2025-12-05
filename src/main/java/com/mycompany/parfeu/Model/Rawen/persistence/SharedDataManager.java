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
 * 🔥 VERSION FINALE CORRIGÉE - Restauration avec nouveau parsing CSV
 */
public final class SharedDataManager {
    
    private static SharedDataManager instance;
    private final StatisticsManager statistics;
    private final StorageManager storage;
    private BlockChain blockchain;
    private FirewallConfig configuration;
    
    private boolean isReconstructing = false;
    
    private SharedDataManager() {
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
            try {
                tempStorage = new StorageManager();
            } catch (DatabaseException ex) {
                System.err.println("⚠️  Impossible de créer StorageManager: " + ex.getMessage());
            }
        } finally {
            this.storage = tempStorage != null ? tempStorage : createDummyStorage();
        }
    }
    
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
            
            // 1️⃣ CONFIGURATION
            System.out.println("📋 1. Configuration...");
            try {
                configuration = storage.loadConfiguration();
                if (configuration != null) {
                    System.out.println("  ✅ Configuration chargée");
                    System.out.println("     - Seuil blocage: " + configuration.getBlockThreshold());
                    System.out.println("     - Seuil alerte: " + configuration.getAlertThreshold());
                } else {
                    configuration = new FirewallConfig();
                    System.out.println("  ℹ️  Configuration par défaut");
                }
            } catch (DatabaseException e) {
                configuration = new FirewallConfig();
                System.out.println("  ⚠️  Configuration par défaut");
            }
            
            // 2️⃣ BLOCKCHAIN (RECONSTRUCTION DEPUIS CSV)
            System.out.println("\n🔗 2. Blockchain...");
            reconstructBlockchainFromCSV(storage);
            
            // 3️⃣ STATISTIQUES
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
     * 🔥 RECONSTRUCTION BLOCKCHAIN - VERSION CORRIGÉE avec BlockData
     */
    private void reconstructBlockchainFromCSV(StorageManager storage) {
        try {
            List<StorageManager.BlockData> blocks = storage.loadBlockHistory();
            
            if (blocks.isEmpty()) {
                System.out.println("  ℹ️  Blockchain vide (genesis uniquement)");
                return;
            }
            
            System.out.println("  📂 " + blocks.size() + " blocs à reconstruire");
            
            int reconstructed = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
            
            for (StorageManager.BlockData blockData : blocks) {
                
                // Skip Genesis (déjà créé)
                if (blockData.index == 0 && "0.0.0.0".equals(blockData.srcIP)) {
                    System.out.println("  ⏭️  Genesis bloc skippé");
                    continue;
                }
                
                try {
                    // Parser le timestamp du paquet
                    LocalDateTime packetTimestamp;
                    try {
                        packetTimestamp = LocalDateTime.parse(blockData.packetTimestamp, formatter);
                    } catch (Exception e) {
                        // Fallback avec format alternatif
                        try {
                            DateTimeFormatter alternativeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                            packetTimestamp = LocalDateTime.parse(blockData.packetTimestamp, alternativeFormatter);
                        } catch (Exception e2) {
                            packetTimestamp = LocalDateTime.now();
                            System.out.println("  ⚠️  Timestamp invalide, utilisation de l'heure actuelle");
                        }
                    }
                    
                    // Créer un paquet pour la décision
                    Packet packet = new PaquetSimple(
                        blockData.srcIP,
                        blockData.destIP,
                        blockData.srcPort,
                        blockData.destPort,
                        blockData.protocol,
                        "Restored from history",
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
                        blockData.index,
                        List.of(decision),
                        blockData.previousHash,
                        blockData.timestamp,
                        blockData.hash,
                        blockData.srcIP,
                        blockData.destIP,
                        blockData.srcPort,
                        blockData.destPort,
                        blockData.protocol,
                        "Restored",
                        blockData.size,
                        packetTimestamp,
                        true  // fromCSV flag
                    );
                    
                    // Ajouter directement à la blockchain
                    blockchain.getChain().add(restoredBlock);
                    
                    // Enregistrer dans les stats
                    statistics.recordDecision(decision);
                    
                    reconstructed++;
                    
                    if (reconstructed <= 3) {
                        System.out.println("  ✓ Bloc #" + blockData.index + " : " + 
                                         blockData.srcIP + " -> " + blockData.destIP + 
                                         " (" + blockData.protocol + ")");
                    }
                    
                } catch (Exception e) {
                    System.err.println("  ⚠️  Erreur reconstruction bloc #" + blockData.index + ": " + e.getMessage());
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