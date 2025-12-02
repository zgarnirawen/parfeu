/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.analyzer;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Signal de détection basé sur des mots/patterns suspects dans le payload.
 * @author ZGARNI
 */
public final class WordPatternSignal implements DetectionSignal {
    private final List<String> foundWords;
    private final int score;

    // Constructeur privé
    private WordPatternSignal(List<String> foundWords, int score) {
        this.foundWords = Collections.unmodifiableList(new ArrayList<>(foundWords));
        this.score = score;
    }

    /**
     * Analyse le payload pour détecter des mots suspects.
     * @param packet paquet à analyser
     * @param suspiciousWords liste des mots à rechercher
     * @return WordPatternSignal si mots trouvés, null sinon
     */
    public static WordPatternSignal analyze(Packet packet, List<String> suspiciousWords) {
        List<String> found = new ArrayList<>();
        String payload = packet.getPayload().toLowerCase();
        
        for (String word : suspiciousWords) {
            if (payload.contains(word.toLowerCase())) {
                found.add(word);
            }
        }
        
        if (!found.isEmpty()) {
            return new WordPatternSignal(found, found.size());
        }
        return null;
    }

    public List<String> getFoundWords() {
        return foundWords;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public String getDescription() {
        return "Mots suspects détectés: " + String.join(", ", foundWords);
    }
}