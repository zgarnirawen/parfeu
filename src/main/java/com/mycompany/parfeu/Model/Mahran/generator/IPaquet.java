/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.generator;
import java.time.LocalDateTime;

/**
 * Interface fonctionnelle pour les paquets réseau.
 * Contient UNE SEULE méthode abstraite : serialize()
 * Toutes les autres méthodes sont default avec accès via getters.
 * 
 * @author ZGARNI
 */
@FunctionalInterface
public interface IPaquet {
    
    /**
     * UNIQUE MÉTHODE ABSTRAITE : sérialise le paquet en bytes.
     * @return représentation binaire du paquet
     */
    byte[] serialize();
    
    // ========== MÉTHODES DEFAULT ==========
    
    /**
     * IP source (doit être overridée par les implémentations).
     */
    default String getSrcIP() {
        return "0.0.0.0";
    }
    
    /**
     * IP destination (doit être overridée par les implémentations).
     */
    default String getDestIP() {
        return "0.0.0.0";
    }
    
    /**
     * Port source (doit être overridé par les implémentations).
     */
    default int getSrcPort() {
        return 0;
    }
    
    /**
     * Port destination (doit être overridé par les implémentations).
     */
    default int getDestPort() {
        return 0;
    }
    
    /**
     * Protocole (doit être overridé par les implémentations).
     */
    default String getProtocol() {
        return "UNKNOWN";
    }
    
    /**
     * Payload (doit être overridé par les implémentations).
     */
    default String getPayload() {
        return "";
    }
    
    /**
     * Taille du paquet (doit être overridée par les implémentations).
     */
    default int getSize() {
        return 0;
    }
    
    /**
     * Timestamp (doit être overridé par les implémentations).
     */
    default LocalDateTime getTimestamp() {
        return LocalDateTime.now();
    }
    
    /**
     * Résumé lisible du paquet.
     */
    default String summary() {
        return String.format("%s %s:%d -> %s:%d [%s] size=%d",
                getTimestamp(),
                getSrcIP(), getSrcPort(),
                getDestIP(), getDestPort(),
                getProtocol(),
                getSize());
    }
}