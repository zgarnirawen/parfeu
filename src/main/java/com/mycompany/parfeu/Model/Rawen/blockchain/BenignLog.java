/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.blockchain;
import java.time.LocalDateTime;

/**
 * Entrée de log normale (non malveillante).
 * Implémente ILogEntry (interface fonctionnelle).
 * Override toutes les méthodes default.
 * 
 * @author ZGARNI
 */
public class BenignLog implements ILogEntry {
    private final String timestamp;
    private final String sourceIP;
    private final String destinationIP;

    public BenignLog(String sourceIP, String destinationIP) {
        this.timestamp = LocalDateTime.now().toString();
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
    }

    @Override
    public String getType() {
        return "benign";
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String getSourceIP() {
        return sourceIP;
    }

    @Override
    public String getDestinationIP() {
        return destinationIP;
    }

    @Override
    public String toString() {
        return "[BenignLog] " + timestamp + " | " + sourceIP + " -> " + destinationIP;
    }
}