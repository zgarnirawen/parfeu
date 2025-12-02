/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.decision;
/**
 * Interface fonctionnelle ET sealed pour les actions du pare-feu.
 * Contient UNE SEULE méthode abstraite : getDescription()
 * Toutes les autres méthodes sont default.
 * 
 * @author ZGARNI
 */
public sealed interface Action 
    permits AcceptAction, DropAction, LogAction, AlertAction {
    
    /**
     * UNIQUE MÉTHODE ABSTRAITE : description de l'action.
     * @return description lisible
     */
    String getDescription();
    
    // ========== MÉTHODES DEFAULT ==========
    
    /**
     * Niveau de sévérité (peut être overridé).
     * @return niveau de 0 (benign) à 10 (critique)
     */
    default int getSeverity() {
        // Détermine la sévérité basée sur le type d'action
        if (this instanceof DropAction) return 10;
        if (this instanceof AlertAction) return 7;
        if (this instanceof LogAction) return 3;
        return 0; // AcceptAction
    }
    
    /**
     * Symbole visuel (peut être overridé).
     * @return symbole pour l'interface
     */
    default String getSymbol() {
        if (this instanceof DropAction) return "[BLOCK]";
        if (this instanceof AlertAction) return "[ALERT]";
        if (this instanceof LogAction) return "[LOG]";
        return "[OK]"; // AcceptAction
    }
    
    /**
     * Vérifie si l'action est critique.
     */
    default boolean isCritical() {
        return getSeverity() >= 7;
    }
    
    /**
     * Couleur pour l'affichage (optionnel).
     */
    default String getColor() {
        if (getSeverity() >= 7) return "RED";
        if (getSeverity() >= 4) return "YELLOW";
        return "GREEN";
    }
}