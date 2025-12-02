/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration centralisee du pare-feu.
 * Contient tous les parametres configurables pour le fonctionnement du pare-feu.
 * 
 * Parametres principaux :
 * - Seuils de decision (blocage, alerte)
 * - Limites de taille des paquets
 * - Liste de mots suspects
 * - IPs blacklistees
 * - Ports a surveiller
 * - Configuration base de donnees
 * 
 * @author ZGARNI
 */
public class FirewallConfig {
    
    // Seuils de decision
    private int blockThreshold;      // Score >= blockThreshold -> BLOQUER
    private int alertThreshold;      // Score >= alertThreshold -> ALERTER
    
    // Limites de taille
    private int minPacketSize;
    private int maxPacketSize;
    
    // Mots suspects
    private List<String> suspiciousWords;
    
    // IPs blacklistees
    private List<String> blacklistedIPs;
    
    // Ports a surveiller
    private List<Integer> monitoredPorts;
    
   

    /**
     * Constructeur avec configuration par defaut.
     */
    public FirewallConfig() {
        this.blockThreshold = 3;
        this.alertThreshold = 2;
        this.minPacketSize = 20;
        this.maxPacketSize = 65535;
        
        // Mots suspects par defaut (SQL injection, XSS, etc.)
        this.suspiciousWords = new ArrayList<>(Arrays.asList(
            "script", "SELECT", "DROP", "INSERT", "UPDATE", "DELETE",
            "UNION", "exec", "eval", "alert", "onerror", "onload",
            "<script>", "javascript:", "../", "etc/passwd"
        ));
        
        this.blacklistedIPs = new ArrayList<>();
        
        this.monitoredPorts = new ArrayList<>(Arrays.asList(
            21, 22, 23, 25, 80, 443, 3306, 3389, 8080
        ));
    }
      
    // Getters
    public int getBlockThreshold() { 
        return blockThreshold; 
    }
    
    public int getAlertThreshold() { 
        return alertThreshold; 
    }
    
    public int getMinPacketSize() { 
        return minPacketSize; 
    }
    
    public int getMaxPacketSize() { 
        return maxPacketSize; 
    }
    
    public List<String> getSuspiciousWords() { 
        return Collections.unmodifiableList(suspiciousWords); 
    }
    
    public List<String> getBlacklistedIPs() { 
        return Collections.unmodifiableList(blacklistedIPs); 
    }
    
    public List<Integer> getMonitoredPorts() { 
        return Collections.unmodifiableList(monitoredPorts); 
    }
    
   
    
    // Setters avec validation
    public void setBlockThreshold(int blockThreshold) {
        if (blockThreshold < 1) {
            throw new IllegalArgumentException("blockThreshold doit etre >= 1");
        }
        this.blockThreshold = blockThreshold;
    }

    public void setAlertThreshold(int alertThreshold) {
        if (alertThreshold < 1) {
            throw new IllegalArgumentException("alertThreshold doit etre >= 1");
        }
        this.alertThreshold = alertThreshold;
    }

    public void setMinPacketSize(int minPacketSize) {
        if (minPacketSize < 0) {
            throw new IllegalArgumentException("minPacketSize doit etre >= 0");
        }
        this.minPacketSize = minPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        if (maxPacketSize <= 0) {
            throw new IllegalArgumentException("maxPacketSize doit etre > 0");
        }
        this.maxPacketSize = maxPacketSize;
    }

    /**
     * Ajoute un mot suspect a la liste.
     * @param word mot a ajouter
     */
    public void addSuspiciousWord(String word) {
        if (word != null && !word.isEmpty()) {
            this.suspiciousWords.add(word);
        }
    }

    /**
     * Supprime un mot suspect de la liste.
     * @param word mot a supprimer
     */
    public void removeSuspiciousWord(String word) {
        this.suspiciousWords.remove(word);
    }

    /**
     * Ajoute une IP a la blacklist.
     * @param ip adresse IP a blacklister
     */
    public void addBlacklistedIP(String ip) {
        if (ip != null && !ip.isEmpty() && !blacklistedIPs.contains(ip)) {
            this.blacklistedIPs.add(ip);
        }
    }

    /**
     * Supprime une IP de la blacklist.
     * @param ip adresse IP a retirer
     */
    public void removeBlacklistedIP(String ip) {
        this.blacklistedIPs.remove(ip);
    }

    /**
     * Ajoute un port a la liste des ports surveilles.
     * @param port numero de port (0-65535)
     */
    public void addMonitoredPort(int port) {
        if (port >= 0 && port <= 65535 && !monitoredPorts.contains(port)) {
            this.monitoredPorts.add(port);
        }
    }

    /**
     * Supprime un port de la liste des ports surveilles.
     * @param port numero de port a retirer
     */
    public void removeMonitoredPort(int port) {
        this.monitoredPorts.remove(Integer.valueOf(port));
    }

    

    @Override
    public String toString() {
        return "FirewallConfig{" +
                "blockThreshold=" + blockThreshold +
                ", alertThreshold=" + alertThreshold +
                ", minPacketSize=" + minPacketSize +
                ", maxPacketSize=" + maxPacketSize +
                ", suspiciousWords=" + suspiciousWords.size() +
                ", blacklistedIPs=" + blacklistedIPs.size() +
                ", monitoredPorts=" + monitoredPorts.size() +
                '}';
    }
}