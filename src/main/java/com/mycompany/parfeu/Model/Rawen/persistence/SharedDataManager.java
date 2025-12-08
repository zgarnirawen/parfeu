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
            
            // Chargement des données
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
     * Chargement complet au démarrage
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
                } else {
                    configuration = new FirewallConfig();
                    System.out.println("  ℹ️  Configuration par défaut");
                }
            } catch (DatabaseException e) {
                configuration = new FirewallConfig();
                System.out.println("  ⚠️  Configuration par défaut");
            }
            
            // 2️⃣ BLOCKCHAIN
            System.out.println("\n🔗 2. Blockchain...");
            reconstructBlockchainFromCSV(storage);
            
            // 3️⃣ STATISTIQUES
            System.out.println("\n📊 3. Statistiques finales...");
            System.out.println("  ✅ Total paquets: " + statistics.getTotalPackets());
            System.out.println("  ✅ Acceptés: " + statistics.getAcceptedPackets());
            System.out.println("  ✅ Bloqués: " + statistics.getDroppedPackets());
            System.out.println("  ✅ Alertes: " + statistics.getAlertedPackets());
            
            isReconstructing = false;
            System.out.println("\n✅ Chargement terminé avec succès!");
            
        } catch (Exception e) {
            isReconstructing = false;
            System.err.println("⚠️  Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 🔥 RECONSTRUCTION BLOCKCHAIN - Restaure le Genesis depuis CSV
     */
    private void reconstructBlockchainFromCSV(StorageManager storage) {
        try {
            List<StorageManager.BlockData> blocks = storage.loadBlockHistory();
            
            if (blocks.isEmpty()) {
                System.out.println("  ℹ️  Aucun historique, création du Genesis");
                blockchain.clear();  // Créera le Genesis
                return;
            }
            
            System.out.println("  📂 " + blocks.size() + " blocs trouvés dans le CSV");
            
            //  DÉMARRER la restauration (vide la chaîne)
            blockchain.startRestoration();
            
            int restored = 0;
            int errors = 0;
            
            for (StorageManager.BlockData blockData : blocks) {
                try {
                    LocalDateTime packetTimestamp = parseTimestamp(blockData.packetTimestamp);
                    
                    //  RESTAURER LE GENESIS tel quel depuis le CSV
                    if (blockData.index == 0) {
                        Block genesisBlock = new Block(
                            blockData.index,
                            new ArrayList<>(),
                            blockData.previousHash,
                            blockData.timestamp,
                            blockData.hash,  // Hash original du CSV
                            blockData.srcIP,
                            blockData.destIP,
                            blockData.srcPort,
                            blockData.destPort,
                            blockData.protocol,
                            blockData.protocol,
                            blockData.size,
                            packetTimestamp,
                            blockData.action,
                            true  // fromCSV
                        );
                        
                        blockchain.restoreBlock(genesisBlock);
                        restored++;
                        continue;
                    }
                    
                    // Créer un paquet pour les autres blocs
                    Packet packet = new PaquetSimple(
                        blockData.srcIP,
                        blockData.destIP,
                        blockData.srcPort,
                        blockData.destPort,
                        blockData.protocol,
                        "Restored from blockchain history",
                        packetTimestamp
                    );
                    
                    // Convertir l'action
                    com.mycompany.parfeu.Model.Rawen.decision.Action action;
                    try {
                        action = Actions.fromString(blockData.action);
                    } catch (IllegalArgumentException e) {
                        System.err.println("  ⚠️  Action invalide: " + blockData.action);
                        action = Actions.LOG;
                    }
                    
                    // Créer la décision
                    DecisionResult decision = new DecisionResult(
                        packet,
                        new ArrayList<>(),
                        0,
                        action,
                        "Restored from blockchain"
                    );
                    
                    // Créer le bloc avec hash original
                    Block restoredBlock = new Block(
                        blockData.index,
                        List.of(decision),
                        blockData.previousHash,
                        blockData.timestamp,
                        blockData.hash,  //  Hash original
                        blockData.srcIP,
                        blockData.destIP,
                        blockData.srcPort,
                        blockData.destPort,
                        blockData.protocol,
                        "Restored",
                        blockData.size,
                        packetTimestamp,
                        blockData.action,
                        true
                    );
                    
                    blockchain.restoreBlock(restoredBlock);
                    statistics.recordDecision(decision);
                    
                    restored++;
                    
                } catch (Exception e) {
                    errors++;
                    System.err.println("  ⚠️  Erreur bloc #" + blockData.index + ": " + e.getMessage());
                }
            }
            
            //  TERMINER la restauration
            blockchain.finishRestoration();
            
            // Rapport
            System.out.println("\n  📊 Rapport de reconstruction:");
            System.out.println("     - Blocs dans CSV: " + blocks.size());
            System.out.println("     - Blocs restaurés: " + restored);
            System.out.println("     - Erreurs: " + errors);
            System.out.println("     - Blockchain.getSize(): " + blockchain.getSize());
            
            //VÉRIFICATION automatique
            boolean valid = blockchain.isChainValid();
            if (valid) {
                System.out.println("  ✅ Blockchain restaurée et VALIDE!");
            } else {
                System.out.println("  ❌ Blockchain restaurée mais INVALIDE!");
                blockchain.printChain();  // Afficher les détails
            }
            
        } catch (DatabaseException e) {
            System.out.println("  ⚠️  Erreur lecture CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Parsing robuste du timestamp
     */
    private LocalDateTime parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
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
        
        System.err.println("  ⚠️  Impossible de parser timestamp: " + timestampStr);
        return LocalDateTime.now();
    }
    
    /**
     * Ajout d'une nouvelle décision
     */
    public void addDecision(DecisionResult decision) {
        if (isReconstructing) {
            blockchain.addDecision(decision);
            statistics.recordDecision(decision);
            return;
        }
        
        try {
            System.out.println("\n💾 Nouvelle décision...");
            System.out.println("   Action: " + decision.getAction());
            
            blockchain.addDecision(decision);
            statistics.recordDecision(decision);
            
            saveAllData();
            
            System.out.println("✅ Décision sauvegardée\n");
            
        } catch (Exception e) {
            System.err.println("✗ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sauvegarde complète
     */
    private void saveAllData() throws DatabaseException {
        List<Block> chain = blockchain.getChain();
        if (!chain.isEmpty()) {
            storage.clearHistory();
            for (Block block : chain) {
                storage.saveBlockToHistory(block);
            }
        }
        
        storage.saveStatistics(statistics);
        
        if (configuration != null) {
            storage.saveConfiguration(configuration);
        }
    }
    
    /**
     * Sauvegarde configuration explicite
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
            blockchain.clear();
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
        System.out.println("   Status        : " + (blockchain.isChainValid() ? "✅ VALIDE" : "❌ INVALIDE"));
        System.out.println("📊 Paquets       : " + statistics.getTotalPackets());
        System.out.println("   ✓ Acceptés    : " + statistics.getAcceptedPackets());
        System.out.println("   ✗ Bloqués     : " + statistics.getDroppedPackets());
        System.out.println("   ⚠ Alertes     : " + statistics.getAlertedPackets());
        System.out.println("⚙️  Configuration :");
        System.out.println("   - Seuil blocage: " + configuration.getBlockThreshold());
        System.out.println("   - Seuil alerte : " + configuration.getAlertThreshold());
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }
}