/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.decision;
import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.generator.PaquetMalicieux;

import java.util.List;
import java.util.Objects;

/**
 * Moteur de decision du pare-feu.
 * Analyse les signaux detectes et decide de l'action a entreprendre
 * selon des regles predefinies et configurables.
 * 
 * REGLES DE DECISION :
 * 1. Si paquet malicieux identifie -> DROP
 * 2. Si score >= blockThreshold -> DROP (bloquer)
 * 3. Si score >= alertThreshold + signal critique -> ALERT
 * 4. Si score >= alertThreshold sans signal critique -> LOG
 * 5. Si score == 0 -> ACCEPT
 * 6. Sinon -> LOG par precaution
 * 
 * Concepts Java utilises :
 * - Streams et Lambda
 * - Method references (DetectionSignal::getScore)
 * - Pattern matching (instanceof)
 * 
 * @author ZGARNI
 */
public final class DecisionEngine {
    
    private final FirewallConfig config;

    public DecisionEngine(FirewallConfig config) {
        this.config = Objects.requireNonNull(config, "config ne peut pas etre null");
    }

    public DecisionResult decide(Packet packet, List<DetectionSignal> signals) {
        Objects.requireNonNull(packet, "packet ne peut pas etre null");
        Objects.requireNonNull(signals, "signals ne peut pas etre null");
        
        int totalScore = calculateTotalScore(signals);
        
        // REGLE 1 : Verifier si c'est un paquet malicieux connu
        if (packet instanceof PaquetMalicieux malicious) {
            return new DecisionResult(
                packet,
                signals,
                Math.max(totalScore, config.getBlockThreshold()),
                Actions.DROP,
                "Paquet malicieux identifie : " + malicious.getTypeAttaque()
            );
        }
        
        // REGLE 2 : Score >= seuil de blocage -> DROP
        if (totalScore >= config.getBlockThreshold()) {
            return new DecisionResult(
                packet,
                signals,
                totalScore,
                Actions.DROP,
                String.format("Score de menace eleve (%d >= %d)", 
                    totalScore, config.getBlockThreshold())
            );
        }
        
        // REGLE 3 & 4 : Score >= seuil d'alerte -> ALERT ou LOG
        if (totalScore >= config.getAlertThreshold()) {
            boolean hasCriticalSignal = signals.stream()
                .anyMatch(signal -> signal.getScore() >= 2);
            
            if (hasCriticalSignal) {
                return new DecisionResult(
                    packet,
                    signals,
                    totalScore,
                    Actions.ALERT,
                    String.format("Menace potentielle detectee (score : %d)", totalScore)
                );
            } else {
                return new DecisionResult(
                    packet,
                    signals,
                    totalScore,
                    Actions.LOG,
                    String.format("Activite suspecte legere (score : %d)", totalScore)
                );
            }
        }
        
        // REGLE 5 : Score == 0 -> ACCEPT
        if (totalScore == 0) {
            return new DecisionResult(
                packet,
                signals,
                0,
                Actions.ACCEPT,
                "Aucune menace detectee - Trafic normal"
            );
        }
        
        // REGLE 6 : Score faible -> LOG par precaution
        return new DecisionResult(
            packet,
            signals,
            totalScore,
            Actions.LOG,
            String.format("Signaux mineurs detectes (score : %d)", totalScore)
        );
    }

    private int calculateTotalScore(List<DetectionSignal> signals) {
        return signals.stream()
            .mapToInt(DetectionSignal::getScore)
            .sum();
    }

    public boolean shouldBlockImmediately(Packet packet) {
        Objects.requireNonNull(packet, "packet ne peut pas etre null");
        
        if (config.getBlacklistedIPs().contains(packet.getSrcIP())) {
            return true;
        }
        
        if (config.getBlacklistedIPs().contains(packet.getDestIP())) {
            return true;
        }
        
        return false;
    }

    public DecisionResult createImmediateBlockResult(Packet packet) {
        return new DecisionResult(
            packet,
            List.of(),
            99,
            Actions.DROP,
            "IP source blacklistee : " + packet.getSrcIP()
        );
    }

    public String evaluateRiskLevel(int score) {
        if (score == 0) return "SAFE";
        if (score < config.getAlertThreshold()) return "LOW";
        if (score < config.getBlockThreshold()) return "MEDIUM";
        if (score < config.getBlockThreshold() + 3) return "HIGH";
        return "CRITICAL";
    }

    public FirewallConfig getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "DecisionEngine{" +
                "blockThreshold=" + config.getBlockThreshold() +
                ", alertThreshold=" + config.getAlertThreshold() +
                '}';
    }
}