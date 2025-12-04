package com.mycompany.parfeu.Model.Rawen.persistence;

import com.mycompany.parfeu.Model.Rawen.blockchain.BlockChain;
import com.mycompany.parfeu.Model.Rawen.statistics.StatisticsManager;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;

/**
 * Gestionnaire partagé des données entre tous les contrôleurs.
 * Pattern Singleton pour assurer une seule instance.
 */
public class SharedDataManager {
    
    private static SharedDataManager instance;
    
    private final BlockChain blockchain;
    private final StatisticsManager statistics;
    
    private SharedDataManager() {
        this.blockchain = new BlockChain();
        this.statistics = new StatisticsManager();
        System.out.println("✓ SharedDataManager initialisé");
    }
    
    /**
     * Obtient l'instance unique du gestionnaire.
     */
    public static synchronized SharedDataManager getInstance() {
        if (instance == null) {
            instance = new SharedDataManager();
        }
        return instance;
    }
    
    /**
     * Ajoute une décision à la blockchain ET aux statistiques.
     */
    public void addDecision(DecisionResult decision) {
        // Ajouter à la blockchain
        blockchain.addDecision(decision);
        
        // Ajouter aux statistiques
        statistics.recordDecision(decision);
        
        System.out.println("✓ Décision ajoutée à la blockchain et aux statistiques");
    }
    
    /**
     * Obtient la blockchain partagée.
     */
    public BlockChain getBlockchain() {
        return blockchain;
    }
    
    /**
     * Obtient le gestionnaire de statistiques partagé.
     */
    public StatisticsManager getStatistics() {
        return statistics;
    }
    
    /**
     * Réinitialise toutes les données (pour tests).
     */
    public void reset() {
        statistics.reset();
        System.out.println("✓ Données réinitialisées");
    }
    
    /**
     * Affiche un résumé des données.
     */
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