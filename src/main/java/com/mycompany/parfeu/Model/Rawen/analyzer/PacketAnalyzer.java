/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.analyzer;

/**
 *
 * @author ZGARNI
 */
import com.mycompany.parfeu.Model.Mahran.generator.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Analyseur de paquets orchestrant tous les types de détection.
 * VERSION SANS analyse par signatures.
 * 
 * @author ZGARNI
 */
public class PacketAnalyzer {
    private final int minSize;
    private final int maxSize;
    private final List<String> suspiciousWords;

    /**
     * Constructeur simplifié (sans signatures).
     * @param minSize taille minimale acceptable
     * @param maxSize taille maximale acceptable
     * @param suspiciousWords liste de mots suspects
     */
    public PacketAnalyzer(int minSize, int maxSize, List<String> suspiciousWords) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.suspiciousWords = new ArrayList<>(
            Objects.requireNonNull(suspiciousWords, "suspiciousWords ne peut pas être null")
        );
        
        System.out.println("✓ Analyseur de paquets initialisé (sans signatures)");
    }

    /**
     * Analyse un paquet avec tous les détecteurs disponibles.
     * @param packet paquet à analyser
     * @return liste de tous les signaux détectés
     */
    public List<DetectionSignal> analyze(Packet packet) {
        List<DetectionSignal> signals = new ArrayList<>();

        // 1. Détection par taille
        SizeSignal sizeSig = SizeSignal.analyze(packet, minSize, maxSize);
        if (sizeSig != null) signals.add(sizeSig);

        // 2. Détection par mots suspects (avec Stream + Lambda)
        WordPatternSignal wordSig = WordPatternSignal.analyze(packet, suspiciousWords);
        if (wordSig != null) signals.add(wordSig);

        // 3. Analyse heuristique
        HeuristicSignal heurSig = HeuristicSignal.analyze(packet);
        if (heurSig != null) signals.add(heurSig);

        return signals;
    }

    /**
     * Calcule le score total de tous les signaux.
     * Utilise Stream + Method Reference.
     * 
     * @param signals liste des signaux détectés
     * @return score total
     */
    public int calculateTotalScore(List<DetectionSignal> signals) {
        return signals.stream()                      // ✅ STREAM
                .mapToInt(DetectionSignal::getScore) // ✅ METHOD REFERENCE
                .sum();                              // ✅ TERMINAL OPERATION
    }
}