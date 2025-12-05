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
 * 🔥 VERSION FINALE CORRIGÉE - Gestionnaire avec reconstruction SANS recalcul de hash
 */
public class SharedDataManager {
    
    private static SharedDataManager instance;
    
    private BlockChain blockchain;
    private final StatisticsManager statistics;
    private final StorageManager storage;
    private FirewallConfig configuration;
    
    // Flag pour éviter de sauvegarder pendant la reconstruction
    private boolean isReconstructing = false;
    
    private SharedDataManager() {
        try {
            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.println("║        INITIALISATION SHARED DATA MANAGER               ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝\n");
            
            this.storage = new StorageManager();
            this.statistics = new StatisticsManager();
            this.blockchain = new BlockChain();
            
            // 🔥 RECONSTRUCTION COMPLÈTE
            loadAllData();
            
            System.out.println("✅ SharedDataManager prêt\n");
            printSummary();
            
        } catch (DatabaseException e) {
            throw new RuntimeException("Erreur init: " + e.getMessage(), e);
        }
    }
    
    public static synchronized SharedDataManager getInstance() {
        if (instance == null) {
            instance = new SharedDataManager();
        }
        return instance;
    }
    
    /**
     * 🔥 AJOUTE une décision (nouveau paquet)
     */
    public void addDecision(DecisionResult decision) {
        if (isReconstructing) {
            // Pendant reconstruction : ajouter SANS sauvegarder
            blockchain.addDecision(decision);
            statistics.recordDecision(decision);
            return;
        }
        
        try {
            System.out.println("\n💾 Nouvelle décision...");
            
            // Ajouter à la blockchain
            blockchain.addDecision(decision);
            System.out.println("  ✓ Blockchain (bloc #" + (blockchain.getSize() - 1) + ")");
            
            // Ajouter aux stats
            statistics.recordDecision(decision);
            System.out.println("  ✓ Statistiques");
            
            // 🔥 SAUVEGARDER
            saveAllData();
            System.out.println("✅ Sauvegardé\n");
            
        } catch (Exception e) {
            System.err.println("✗ Erreur: " + e.getMessage());
        }
    }
    
    /**
     * 🔥 CHARGEMENT COMPLET au démarrage
     */
    private void loadAllData() {
        System.out.println("🔄 Restauration des données...\n");
        
        try {
            isReconstructing = true;
            
            // 1. Configuration
            loadConfiguration();
            
            // 2. 🔥 RECONSTRUCTION BLOCKCHAIN SANS RECALCUL DE HASH
            reconstructBlockchainFromCSV();
            
            // 3. Stats (déjà OK via reconstruction)
            System.out.println("\n📊 Statistiques: " + statistics.getTotalPackets() + " paquets");
            
            isReconstructing = false;
            System.out.println("\n✅ Restauration terminée");
            
        } catch (Exception e) {
            isReconstructing = false;
            System.out.println("⚠️  Première utilisation\n");
        }
    }
    
    /**
     * Configuration
     */
    private void loadConfiguration() {
        try {
            System.out.println("📋 Configuration...");
            configuration = storage.loadConfiguration();
            System.out.println("  ✓ Chargée");
        } catch (DatabaseException e) {
            System.out.println("  ℹ️  Défaut");
            configuration = new FirewallConfig();
        }
    }
    
    /**
     * 🔥 RECONSTRUCTION BLOCKCHAIN depuis historique_blocs.csv
     * SANS RECALCULER LES HASH
     */
    private void reconstructBlockchainFromCSV() {
        try {
            System.out.println("\n🔗 Blockchain...");
            
            List<String> lines = storage.loadBlockHistory();
            
            if (lines.isEmpty()) {
                System.out.println("  ℹ️  Vide (genesis uniquement)");
                System.out.println("  📊 Taille: " + blockchain.getSize() + " bloc");
                return;
            }
            
            System.out.println("  📂 " + lines.size() + " lignes dans CSV");
            
            int reconstructed = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS");
            
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("Index,")) {
                    continue; // Skip header
                }
                
                try {
                    String[] parts = line.split(",");
                    
                    if (parts.length < 11) {
                        System.err.println("  ⚠️  Ligne invalide (trop courte): " + parts.length + " parties");
                        continue;
                    }
                    
                    int index = Integer.parseInt(parts[0].trim());
                    
                    // 🔥 NE PAS RECONSTRUIRE le Genesis (déjà créé)
                    String srcIP = parts[1].trim();
                    if (index == 0 && "0.0.0.0".equals(srcIP)) {
                        System.out.println("  ⏭️  Genesis skippé (déjà créé)");
                        continue;
                    }
                    
                    // 🔥 EXTRAIRE TOUTES LES DONNÉES DU CSV
                    String destIP = parts[2].trim();
                    int srcPort = Integer.parseInt(parts[3].trim());
                    int destPort = Integer.parseInt(parts[4].trim());
                    String protocol = parts[5].trim();
                    int size = Integer.parseInt(parts[6].trim());
                    long blockTimestamp = Long.parseLong(parts[7].trim());
                    
                    // Parse packet timestamp
                    LocalDateTime packetTimestamp;
                    try {
                        packetTimestamp = LocalDateTime.parse(parts[8].trim(), formatter);
                    } catch (Exception e) {
                        packetTimestamp = LocalDateTime.now();
                    }
                    
                    String previousHash = parts[9].trim();
                    String hash = parts[10].trim();  // 🔥 HASH ORIGINAL depuis CSV
                    
                    System.out.println("  📦 Bloc #" + index + " : " + srcIP + ":" + srcPort + 
                                     " -> " + destIP + ":" + destPort + " (" + protocol + ")");
                    System.out.println("     Hash: " + hash.substring(0, Math.min(16, hash.length())) + "...");
                    
                    // Créer un paquet pour la décision
                    Packet packet = new PaquetSimple(
                        srcIP, destIP, srcPort, destPort,
                        protocol, "Données restaurées depuis CSV", 
                        packetTimestamp
                    );
                    
                    DecisionResult decision = new DecisionResult(
                        packet,
                        new ArrayList<>(),
                        0,
                        Actions.LOG,
                        "Restauré depuis historique CSV"
                    );
                    
                    // 🔥 CRÉER LE BLOC AVEC LE HASH ORIGINAL
                    Block restoredBlock = new Block(
                        index,
                        List.of(decision),
                        previousHash,
                        blockTimestamp,
                        hash,  // 🔥 HASH ORIGINAL (pas recalculé)
                        srcIP,
                        destIP,
                        srcPort,
                        destPort,
                        protocol,
                        "Restauré depuis CSV",
                        size,
                        packetTimestamp,
                        true  // 🔥 Flag fromCSV = true
                    );
                    
                    // 🔥 AJOUTER DIRECTEMENT LE BLOC À LA BLOCKCHAIN
                    blockchain.getChain().add(restoredBlock);
                    
                    // Enregistrer dans les stats
                    statistics.recordDecision(decision);
                    
                    reconstructed++;
                    
                } catch (Exception e) {
                    System.err.println("  ⚠️  Erreur ligne: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("  ✓ " + reconstructed + " blocs reconstruits");
            System.out.println("  📊 Taille totale: " + blockchain.getSize() + " blocs");
            
        } catch (DatabaseException e) {
            System.out.println("  ⚠️  Pas d'historique trouvé");
        }
    }
    
    /**
     * Sauvegarde tout
     */
    /**
 * 🔥 SAUVEGARDE COMPLÈTE : tous les blocs, stats et config
 */
private void saveAllData() throws DatabaseException {
    // 1️⃣ Sauvegarder tous les blocs
    List<Block> chain = blockchain.getChain();
    if (!chain.isEmpty()) {
        // Effacer l'historique existant pour éviter doublons
        storage.clearHistory();
        for (Block block : chain) {
            storage.saveBlockToHistory(block);
        }
        System.out.println("✓ Tous les blocs sauvegardés (" + chain.size() + ")");
    } else {
        System.out.println("⚠ Aucune blockchain à sauvegarder");
    }

    // 2️⃣ Sauvegarder les statistiques
    storage.saveStatistics(statistics);

    // 3️⃣ Sauvegarder la configuration
    if (configuration != null) {
        storage.saveConfiguration(configuration);
    }

    System.out.println("💾 Sauvegarde complète terminée");
}

    /**
     * Sauvegarde configuration
     */
    public void saveConfiguration(FirewallConfig config) {
        try {
            this.configuration = config;
            storage.saveConfiguration(config);
            System.out.println("✓ Configuration sauvegardée");
        } catch (DatabaseException e) {
            System.err.println("✗ Erreur config: " + e.getMessage());
        }
    }
    
    // Getters
    public BlockChain getBlockchain() { return blockchain; }
    public StatisticsManager getStatistics() { return statistics; }
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
        }
    }
    
    /**
     * Résumé console
     */
    public void printSummary() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                 RÉSUMÉ                                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("🔗 Blockchain    : " + blockchain.getSize() + " blocs");
        System.out.println("📊 Paquets       : " + statistics.getTotalPackets());
        System.out.println("   ✓ Acceptés    : " + statistics.getAcceptedPackets());
        System.out.println("   ✗ Bloqués     : " + statistics.getDroppedPackets());
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }
}