/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.blockchain;

/**
 * Interface fonctionnelle pour les entrées de logs.
 * Contient UNE SEULE méthode abstraite : getType()
 * Toutes les autres méthodes sont default.
 * 
 * @author ZGARNI
 */
@FunctionalInterface
public interface ILogEntry {
    
    /**
     * UNIQUE MÉTHODE ABSTRAITE : type de log.
     * @return "benign" ou "malicious"
     */
    String getType();
    
    // ========== MÉTHODES DEFAULT ==========
    
    /**
     * Timestamp du log (doit être overridé).
     */
    default String getTimestamp() {
        return java.time.LocalDateTime.now().toString();
    }
    
    /**
     * IP source (doit être overridée).
     */
    default String getSourceIP() {
        return "0.0.0.0";
    }
    
    /**
     * IP destination (doit être overridée).
     */
    default String getDestinationIP() {
        return "0.0.0.0";
    }
    
    /**
     * Représentation textuelle (peut être overridée).
     */
    default String toLogString() {
        return String.format("[%s] %s | %s -> %s",
            getType(),
            getTimestamp(),
            getSourceIP(),
            getDestinationIP());
    }
}
