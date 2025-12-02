/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.blockchain;
import java.time.LocalDateTime;

/**
 * Entrée de log malveillante.
 * Implémente ILogEntry (interface fonctionnelle).
 * Override toutes les méthodes default.
 * 
 * @author ZGARNI
 */
public class MaliciousLog implements ILogEntry {
    private final String timestamp;
    private final String sourceIP;
    private final String destinationIP;
    private final String threatType;

    public MaliciousLog(String sourceIP, String destinationIP, String threatType) {
        this.timestamp = LocalDateTime.now().toString();
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.threatType = threatType;
    }

    public String getThreatType() {
        return threatType;
    }

    @Override
    public String getType() {
        return "malicious";
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
        return "[MaliciousLog] " + timestamp + " | " + sourceIP + " -> " + 
               destinationIP + " | Type: " + threatType;
    }
}