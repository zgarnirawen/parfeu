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
 * 🔥 VERSION FINALE CORRIGÉE - Parsing CSV robuste avec détection des problèmes
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
     * 🔥 RECONSTRUCTION BLOCKCHAIN - VERSION ULTRA-ROBUSTE
     */
    private void reconstructBlockchainFromCSV(StorageManager storage) {
        try {
            // 🔥 UTILISER LA NOUVELLE MÉTHODE DE PARSING ROBUSTE
            List<StorageManager.BlockData> blocks = storage.loadBlockHistory();
            
            if (blocks.isEmpty()) {
                System.out.println("  ℹ️  Blockchain vide (genesis uniquement)");
                return;
            }
            
            System.out.println("  📂 " + blocks.size() + " blocs trouvés dans le CSV");
            
            int reconstructed = 0;
            int skipped = 0;
            int errors = 0;
            
            for (StorageManager.BlockData blockData : blocks) {
                
                // Skip Genesis (déjà créé par BlockChain constructor)
                if (blockData.index == 0 && "0.0.0.0".equals(blockData.srcIP)) {
                    System.out.println("  ⏭️  Genesis bloc skippé (index 0)");
                    skipped++;
                    continue;
                }
                
                try {
                    // 🔥 PARSING DU TIMESTAMP AVEC PLUSIEURS FORMATS
                    LocalDateTime packetTimestamp = parseTimestamp(blockData.packetTimestamp);
                    
                    // Créer un paquet pour la décision
                    Packet packet = new PaquetSimple(
                        blockData.srcIP,
                        blockData.destIP,
                        blockData.srcPort,
                        blockData.destPort,
                        blockData.protocol,
                        "Restored from blockchain history",
                        packetTimestamp
                    );
                    
                    // Créer une décision fictive pour les stats
                    DecisionResult decision = new DecisionResult(
                        packet,
                        new ArrayList<>(),
                        0,
                        Actions.LOG,
                        "Restored from blockchain"
                    );
                    
                    // 🔥 CRÉER LE BLOC AVEC LE HASH ORIGINAL (fromCSV = true)
                    Block restoredBlock = new Block(
                        blockData.index,
                        List.of(decision),
                        blockData.previousHash,
                        blockData.timestamp,
                        blockData.hash,          // 🔥 HASH ORIGINAL
                        blockData.srcIP,
                        blockData.destIP,
                        blockData.srcPort,
                        blockData.destPort,
                        blockData.protocol,
                        "Restored",
                        blockData.size,
                        packetTimestamp,
                        true  // 🔥 fromCSV = true (ne pas recalculer)
                    );
                    
                    // 🔥 UTILISER LA NOUVELLE MÉTHODE restoreBlock()
                    blockchain.restoreBlock(restoredBlock);
                    
                    // Enregistrer dans les stats
                    statistics.recordDecision(decision);
                    
                    reconstructed++;
                    
                    // Afficher les 3 premiers blocs
                    if (reconstructed <= 3) {
                        System.out.println("  ✓ Bloc #" + blockData.index + " : " + 
                                         blockData.srcIP + " -> " + blockData.destIP + 
                                         " (" + blockData.protocol + ")");
                    }
                    
                } catch (Exception e) {
                    errors++;
                    System.err.println("  ⚠️  Erreur bloc #" + blockData.index + ": " + e.getMessage());
                }
            }
            
            // 🔥 RAPPORT DÉTAILLÉ
            System.out.println("\n  📊 Rapport de reconstruction:");
            System.out.println("     - Blocs dans CSV: " + blocks.size());
            System.out.println("     - Blocs skippés (genesis): " + skipped);
            System.out.println("     - Blocs reconstruits: " + reconstructed);
            System.out.println("     - Erreurs: " + errors);
            System.out.println("     - Blockchain.getSize(): " + blockchain.getSize() + " blocs");
            System.out.println("     - Blockchain.getChain().size(): " + blockchain.getChain().size() + " blocs");
            
            if (reconstructed > 0) {
                System.out.println("  ✅ Blockchain restaurée avec succès!");
            } else {
                System.out.println("  ⚠️  Aucun bloc restauré (vérifiez le format CSV)");
            }
            
        } catch (DatabaseException e) {
            System.out.println("  ⚠️  Erreur lecture CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 🔥 PARSING ROBUSTE DU TIMESTAMP avec plusieurs formats
     */
    private LocalDateTime parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        // Liste des formats à essayer (du plus précis au moins précis)
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(timestampStr, formatter);
            } catch (Exception e) {
                // Essayer le format suivant
            }
        }
        
        // Fallback : timestamp actuel
        System.err.println("  ⚠️  Impossible de parser timestamp: " + timestampStr);
        return LocalDateTime.now();
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