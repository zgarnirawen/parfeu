/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.analyzer;

/**
 *
 * @author ZGARNI
 */

/**
 * Interface fonctionnelle pour les signaux de détection.
 * Contient UNE SEULE méthode abstraite.
 * 
 * @author ZGARNI
 */
@FunctionalInterface
public interface DetectionSignal {
    
    /**
     * Retourne le score de menace du signal.
     * UNIQUE MÉTHODE ABSTRAITE.
     * 
     * @return score de 0 (bénin) à 10 (critique)
     */
    int getScore();
    
    /**
     * Méthode default : description du signal.
     * Les implémentations DOIVENT override cette méthode pour fournir
     * une description significative.
     * 
     * @return description par défaut
     */
    default String getDescription() {
        return "Signal détecté avec score: " + getScore();
    }
    
    /**
     * Méthode default : vérifie si le signal est critique.
     * @return true si score >= 5
     */
    default boolean isCritical() {
        return getScore() >= 5;
    }
    
    /**
     * Méthode default : niveau de menace.
     * @return LOW, MEDIUM, HIGH, CRITICAL
     */
    default String getThreatLevel() {
        int score = getScore();
        if (score == 0) return "SAFE";
        if (score <= 2) return "LOW";
        if (score <= 4) return "MEDIUM";
        if (score <= 7) return "HIGH";
        return "CRITICAL";
    }
}

