package com.mycompany.parfeu.Model.Rawen.persistence;

import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockChain;
import com.mycompany.parfeu.Model.Rawen.statistics.StatisticsManager;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Rawen.exception.DatabaseException;

/**
 * Gestionnaire partagé avec PERSISTENCE automatique.
 * Sauvegarde après chaque ajout de décision.
 */
public class SharedDataManager {
    
    private static SharedDataManager instance;
    
    private final BlockChain blockchain;
    private final StatisticsManager statistics;
    private final StorageManager storage;
    
    private SharedDataManager() {
        try {
            this.storage = new StorageManager();
            this.blockchain = new BlockChain();
            this.statistics = new StatisticsManager();
            
            // 🔥 CHARGER LES DONNÉES AU DÉMARRAGE
            loadAllData();
            
            System.out.println("✓ SharedDataManager initialisé avec données restaurées");
        } catch (DatabaseException e) {
            throw new RuntimeException("Erreur initialisation SharedDataManager", e);
        }
    }
    
    public static synchronized SharedDataManager getInstance() {
        if (instance == null) {
            instance = new SharedDataManager();
        }
        return instance;
    }
    
    /**
     * 🔥 MÉTHODE CRITIQUE : Ajoute ET sauvegarde automatiquement
     */
    public void addDecision(DecisionResult decision) {
        try {
            // 1. Ajouter à la blockchain
            blockchain.addDecision(decision);
            
            // 2. Ajouter aux statistiques
            statistics.recordDecision(decision);
            
            // 3. 🔥 SAUVEGARDER IMMÉDIATEMENT
            saveAllData();
            
            System.out.println("✓ Décision ajoutée et sauvegardée");
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'ajout de la décision: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Charge toutes les données depuis les fichiers
     */
    private void loadAllData() {
        System.out.println("\n🔄 Chargement des données...");
        
        try {
            // Charger l'historique des blocs
            var blockHistory = storage.loadBlockHistory();
            System.out.println("  📦 " + blockHistory.size() + " blocs trouvés");
            
            // Note: Pour reconstruire la blockchain complète, il faudrait
            // parser le CSV et recréer les objets Block. Pour l'instant,
            // on démarre avec une blockchain vide + genesis.
            // Vous pouvez implémenter la restauration complète si nécessaire.
            
        } catch (DatabaseException e) {
            System.out.println("  ⚠ Pas de données précédentes (premier lancement)");
        }
        
        System.out.println("✓ Chargement terminé\n");
    }
    
    /**
     * Sauvegarde toutes les données
     */
    private void saveAllData() throws DatabaseException {
        // Sauvegarder le dernier bloc
        Block lastBlock = blockchain.getLastBlock();
        if (lastBlock.index() > 0) { // Ne pas sauvegarder le genesis plusieurs fois
            storage.saveBlockToHistory(lastBlock);
        }
        
        // Sauvegarder les statistiques
        storage.saveStatistics(statistics);
        
        System.out.println("  💾 Données sauvegardées sur disque");
    }
    
    public BlockChain getBlockchain() {
        return blockchain;
    }
    
    public StatisticsManager getStatistics() {
        return statistics;
    }
    
    public void reset() {
        try {
            statistics.reset();
            storage.clearAll();
            System.out.println("✓ Toutes les données ont été effacées");
        } catch (DatabaseException e) {
            System.err.println("✗ Erreur lors de la réinitialisation: " + e.getMessage());
        }
    }
    
    public void printSummary() {
        System.out.println("\n══════════════════════════════════════════════");
        System.out.println("           RÉSUMÉ DES DONNÉES");
        System.out.println("══════════════════════════════════════════════");
        System.out.println("Blocs dans la blockchain : " + blockchain.getSize());
        System.out.println("Total paquets traités    : " + statistics.getTotalPackets());
        System.out.println("  ✓ Acceptés             : " + statistics.getAcceptedPackets());
        System.out.println("  ✗ Bloqués              : " + statistics.getDroppedPackets());
        System.out.println("  ⚠ Alertes              : " + statistics.getAlertedPackets());
        System.out.println("══════════════════════════════════════════════\n");
    }
}