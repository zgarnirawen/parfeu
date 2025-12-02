/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.statistics;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Rawen.decision.Actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestionnaire centralisé des statistiques du pare-feu.
 * Collecte et analyse toutes les métriques du système.
 * 
 * @author ZGARNI
 */
public class StatisticsManager {
    
    // Compteurs atomiques (thread-safe)
    private final AtomicInteger totalPackets = new AtomicInteger(0);
    private final AtomicInteger acceptedPackets = new AtomicInteger(0);
    private final AtomicInteger droppedPackets = new AtomicInteger(0);
    private final AtomicInteger alertedPackets = new AtomicInteger(0);
    private final AtomicInteger loggedPackets = new AtomicInteger(0);
    
    // Historique des décisions
    private final List<DecisionResult> decisionHistory = new CopyOnWriteArrayList<>();
    
    // Statistiques par IP
    private final Map<String, IPStatistics> ipStats = new HashMap<>();
    
    // Statistiques par protocole
    private final Map<String, ProtocolStatistics> protocolStats = new HashMap<>();
    
    // Timestamp de démarrage
    private long startTime;
    
    /**
     * Constructeur.
     */
    public StatisticsManager() {
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Réinitialise toutes les statistiques.
     */
    public void reset() {
        totalPackets.set(0);
        acceptedPackets.set(0);
        droppedPackets.set(0);
        alertedPackets.set(0);
        loggedPackets.set(0);
        decisionHistory.clear();
        ipStats.clear();
        protocolStats.clear();
        startTime = System.currentTimeMillis();
    }
    
    /**
     * Enregistre une nouvelle décision et met à jour les statistiques.
     */
    public void recordDecision(DecisionResult decision) {
        totalPackets.incrementAndGet();
        
        // Mise à jour des compteurs par action
        if (decision.getAction() == Actions.ACCEPT) {
            acceptedPackets.incrementAndGet();
        } else if (decision.getAction() == Actions.DROP) {
            droppedPackets.incrementAndGet();
        } else if (decision.getAction() == Actions.ALERT) {
            alertedPackets.incrementAndGet();
        } else if (decision.getAction() == Actions.LOG) {
            acceptedPackets.incrementAndGet();
            loggedPackets.incrementAndGet();
        }
        
        // Ajout à l'historique (limité à 1000 entrées)
        decisionHistory.add(decision);
        if (decisionHistory.size() > 1000) {
            decisionHistory.remove(0);
        }
        
        // Mise à jour statistiques IP
        updateIPStatistics(decision);
        
        // Mise à jour statistiques protocole
        updateProtocolStatistics(decision);
    }
    
    /**
     * Met à jour les statistiques par IP.
     */
    private void updateIPStatistics(DecisionResult decision) {
        String srcIP = decision.getPacket().getSrcIP();
        
        ipStats.putIfAbsent(srcIP, new IPStatistics(srcIP));
        IPStatistics stats = ipStats.get(srcIP);
        
        stats.totalPackets++;
        if (decision.isBlocked()) {
            stats.blockedPackets++;
        } else if (decision.isAccepted()) {
            stats.acceptedPackets++;
        }
        stats.totalScore += decision.getTotalScore();
    }
    
    /**
     * Met à jour les statistiques par protocole.
     */
    private void updateProtocolStatistics(DecisionResult decision) {
        String protocol = decision.getPacket().getProtocol();
        
        protocolStats.putIfAbsent(protocol, new ProtocolStatistics(protocol));
        ProtocolStatistics stats = protocolStats.get(protocol);
        
        stats.totalPackets++;
        if (decision.isBlocked()) {
            stats.blockedPackets++;
        } else if (decision.isAccepted()) {
            stats.acceptedPackets++;
        }
    }
    
    /**
     * Affiche un rapport complet des statistiques.
     */
    public void printFullReport() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              RAPPORT STATISTIQUES COMPLET                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        printBasicStatistics();
        printIPStatistics();
        printProtocolStatistics();
        printPerformanceMetrics();
    }
    
    /**
     * Affiche les statistiques de base.
     */
    public void printBasicStatistics() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📊 STATISTIQUES GÉNÉRALES");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Total paquets traités : " + totalPackets.get());
        System.out.println("  ✓ Acceptés          : " + acceptedPackets.get());
        System.out.println("  ✗ Bloqués           : " + droppedPackets.get());
        System.out.println("  ⚠ Alertes           : " + alertedPackets.get());
        System.out.println("  📝 Journalisés      : " + loggedPackets.get());
        
        if (totalPackets.get() > 0) {
            double blockRate = (droppedPackets.get() * 100.0) / totalPackets.get();
            double alertRate = (alertedPackets.get() * 100.0) / totalPackets.get();
            double acceptRate = (acceptedPackets.get() * 100.0) / totalPackets.get();
            
            System.out.println("\n📈 TAUX");
            System.out.printf("  Acceptation : %.2f%%\n", acceptRate);
            System.out.printf("  Blocage     : %.2f%%\n", blockRate);
            System.out.printf("  Alerte      : %.2f%%\n", alertRate);
        }
    }
    
    /**
     * Affiche les statistiques par IP.
     */
    public void printIPStatistics() {
        if (ipStats.isEmpty()) {
            return;
        }
        
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🌐 STATISTIQUES PAR IP SOURCE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Trier par nombre de paquets bloqués (les plus suspects en premier)
        ipStats.values().stream()
            .sorted((a, b) -> Integer.compare(b.blockedPackets, a.blockedPackets))
            .limit(10) // Top 10
            .forEach(stats -> {
                double blockRate = stats.totalPackets > 0 
                    ? (stats.blockedPackets * 100.0) / stats.totalPackets 
                    : 0;
                double avgScore = stats.totalPackets > 0
                    ? (double) stats.totalScore / stats.totalPackets
                    : 0;
                
                String threat = blockRate > 50 ? "🔴 ÉLEVÉ" : 
                               blockRate > 20 ? "🟡 MOYEN" : "🟢 FAIBLE";
                
                System.out.printf("  %s | Total: %d | Bloqués: %d (%.1f%%) | Score moy: %.1f | %s\n",
                    stats.ipAddress, stats.totalPackets, stats.blockedPackets, 
                    blockRate, avgScore, threat);
            });
    }
    
    /**
     * Affiche les statistiques par protocole.
     */
    public void printProtocolStatistics() {
        if (protocolStats.isEmpty()) {
            return;
        }
        
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📡 STATISTIQUES PAR PROTOCOLE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        protocolStats.values().forEach(stats -> {
            double blockRate = stats.totalPackets > 0 
                ? (stats.blockedPackets * 100.0) / stats.totalPackets 
                : 0;
            
            System.out.printf("  %s : %d paquets | Bloqués: %d (%.1f%%)\n",
                stats.protocol, stats.totalPackets, stats.blockedPackets, blockRate);
        });
    }
    
    /**
     * Affiche les métriques de performance.
     */
    public void printPerformanceMetrics() {
        long uptimeMs = System.currentTimeMillis() - startTime;
        long uptimeSec = uptimeMs / 1000;
        
        double packetsPerSecond = uptimeSec > 0 
            ? (double) totalPackets.get() / uptimeSec 
            : 0;
        
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("⚡ MÉTRIQUES DE PERFORMANCE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("  Temps d'exécution  : %d secondes\n", uptimeSec);
        System.out.printf("  Débit              : %.2f paquets/seconde\n", packetsPerSecond);
        System.out.printf("  Historique stocké  : %d décisions\n", decisionHistory.size());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
    
    // Getters
    public int getTotalPackets() { return totalPackets.get(); }
    public int getAcceptedPackets() { return acceptedPackets.get(); }
    public int getDroppedPackets() { return droppedPackets.get(); }
    public int getAlertedPackets() { return alertedPackets.get(); }
    public int getLoggedPackets() { return loggedPackets.get(); }
    public List<DecisionResult> getDecisionHistory() { return new ArrayList<>(decisionHistory); }
    public Map<String, IPStatistics> getIPStatistics() { return new HashMap<>(ipStats); }
    
    /**
     * Classe interne pour statistiques par IP.
     */
    public static class IPStatistics {
        public final String ipAddress;
        public int totalPackets;
        public int acceptedPackets;
        public int blockedPackets;
        public int totalScore;
        
        public IPStatistics(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }
    
    /**
     * Classe interne pour statistiques par protocole.
     */
    public static class ProtocolStatistics {
        public final String protocol;
        public int totalPackets;
        public int acceptedPackets;
        public int blockedPackets;
        
        public ProtocolStatistics(String protocol) {
            this.protocol = protocol;
        }
    }
}