/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.engine;


import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Rawen.analyzer.PacketAnalyzer;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Rawen.decision.Actions;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionEngine;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.initialPacketFiltering.AbstractFilter;
import com.mycompany.parfeu.Model.Rawen.statistics.StatisticsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Moteur principal du pare-feu.
 * VERSION SANS analyse par signatures.
 * 
 * Détections disponibles :
 * - Taille anormale
 * - Mots suspects
 * - Heuristique (comportement)
 * 
 * @author ZGARNI
 */
public final class FirewallEngine {
    
    private final FirewallConfig config;
    private final PacketAnalyzer analyzer;
    private final DecisionEngine decisionEngine;
    private final List<AbstractFilter> filters;
    private final StatisticsManager statistics;
    
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Constructeur avec configuration.
     */
    public FirewallEngine(FirewallConfig config) {
        this.config = Objects.requireNonNull(config, "config ne peut pas être null");
        this.filters = new ArrayList<>();
        this.statistics = new StatisticsManager();
        
        // Initialiser l'analyseur SANS signatures
        this.analyzer = new PacketAnalyzer(
            config.getMinPacketSize(),
            config.getMaxPacketSize(),
            config.getSuspiciousWords()
        );
        
        this.decisionEngine = new DecisionEngine(config);
        
        System.out.println("✓ Pare-feu initialisé (mode sans signatures)");
    }

    /**
     * Constructeur par défaut.
     */
    public FirewallEngine() {
        this(new FirewallConfig());
    }

    public void addFilter(AbstractFilter filter) {
        Objects.requireNonNull(filter, "filter ne peut pas être null");
        filters.add(filter);
        System.out.println("✓ Filtre ajouté : " + filter.getClass().getSimpleName());
    }

    public void clearFilters() {
        filters.clear();
        System.out.println("Tous les filtres ont été supprimés");
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         PARE-FEU INTELLIGENT DÉMARRÉ                         ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("Configuration : " + config);
            System.out.println("Filtres actifs : " + filters.size());
            System.out.println("Mode : Sans signatures (3 détections actives)");
            System.out.println("  - Détection par taille");
            System.out.println("  - Détection par mots suspects");
            System.out.println("  - Analyse heuristique");
            System.out.println("Prêt à traiter les paquets\n");
        } else {
            System.out.println("Le pare-feu est déjà démarré");
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            System.out.println("\nArrêt du pare-feu...");
            System.out.println("Pare-feu arrêté");
            printStatistics();
        } else {
            System.out.println("Le pare-feu est déjà arrêté");
        }
    }

    /**
     * Traite un paquet.
     * Workflow :
     * 1. Filtrage initial
     * 2. Vérification blacklist
     * 3. Analyse (taille + mots + heuristique)
     * 4. Décision
     * 5. Statistiques
     */
    public DecisionResult processPacket(Packet packet) {
        Objects.requireNonNull(packet, "packet ne peut pas être null");
        
        if (!running.get()) {
            throw new IllegalStateException("Le pare-feu n'est pas démarré. Appelez start() d'abord.");
        }

        // ÉTAPE 1 : Filtrage initial
        for (AbstractFilter filter : filters) {
            if (!filter.accept(packet)) {
                DecisionResult result = new DecisionResult(
                    packet,
                    List.of(),
                    0,
                    Actions.DROP,
                    "Bloqué par filtre : " + filter.getClass().getSimpleName()
                );
                statistics.recordDecision(result);
                return result;
            }
        }

        // ÉTAPE 2 : Vérification blacklist
        if (decisionEngine.shouldBlockImmediately(packet)) {
            DecisionResult result = decisionEngine.createImmediateBlockResult(packet);
            statistics.recordDecision(result);
            logDecision(result);
            return result;
        }

        // ÉTAPE 3 : Analyse approfondie (3 détections)
        List<DetectionSignal> signals = analyzer.analyze(packet);

        // ÉTAPE 4 : Décision basée sur les signaux
        DecisionResult result = decisionEngine.decide(packet, signals);

        // ÉTAPE 5 : Enregistrement statistiques
        statistics.recordDecision(result);
        
        // ÉTAPE 6 : Logger si nécessaire
        if (!(result.getAction() == Actions.ACCEPT)) {
            logDecision(result);
        }

        return result;
    }

    /**
     * Traite plusieurs paquets.
     * Utilise Stream + Method Reference.
     */
    public List<DecisionResult> processPackets(List<Packet> packets) {
        Objects.requireNonNull(packets, "packets ne peut pas être null");
        
        return packets.stream()                      // ✅ STREAM
            .map(this::processPacket)                // ✅ METHOD REFERENCE
            .toList();                               // ✅ COLLECTOR
    }

    private void logDecision(DecisionResult result) {
        System.out.println(result);
        
        if (result.needsAlert()) {
            System.out.println(result.getDetailedSummary());
        }
    }

    public void printStatistics() {
        statistics.printFullReport();
    }

    // Getters
    public boolean isRunning() { return running.get(); }
    public FirewallConfig getConfig() { return config; }
    public StatisticsManager getStatistics() { return statistics; }
    public int getTotalPackets() { return statistics.getTotalPackets(); }
    public int getAcceptedPackets() { return statistics.getAcceptedPackets(); }
    public int getDroppedPackets() { return statistics.getDroppedPackets(); }
    public int getAlertedPackets() { return statistics.getAlertedPackets(); }
    public int getLoggedPackets() { return statistics.getLoggedPackets(); }
    
    public List<DecisionResult> getDecisionHistory() { 
        return statistics.getDecisionHistory();
    }
    
    public List<AbstractFilter> getFilters() { 
        return new ArrayList<>(filters); 
    }
}