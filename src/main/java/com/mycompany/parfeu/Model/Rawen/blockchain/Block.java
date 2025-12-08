package com.mycompany.parfeu.Model.Rawen.blockchain;

import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Block immuable avec informations complètes des paquets.
 */
public record Block(
    int index,
    List<DecisionResult> decisions,
    String previousHash,
    long timestamp,
    String hash,
    // Informations supplémentaires pour l'historique
    String srcIP,
    String destIP,
    int srcPort,
    int destPort,
    String protocol,
    String payload,
    int size,
    LocalDateTime packetTimestamp,
    String action  // Stocker l'action (ACCEPT, DROP, ALERT, LOG)
) {

    /**
     *  Constructeur pour RESTAURATION depuis CSV
     * Accepte le hash existant SANS le recalculer
     */
    public Block(
        int index,
        List<DecisionResult> decisions,
        String previousHash,
        long timestamp,
        String hash,
        String srcIP,
        String destIP,
        int srcPort,
        int destPort,
        String protocol,
        String payload,
        int size,
        LocalDateTime packetTimestamp,
        String action,
        boolean fromCSV
    ) {
        this(
            index,
            Collections.unmodifiableList(decisions),
            previousHash,
            timestamp,
            hash,
            srcIP,
            destIP,
            srcPort,
            destPort,
            protocol,
            payload,
            size,
            packetTimestamp,
            action
        );
    }

    /**
     * Constructeur pour créer un nouveau bloc avec décisions.
     */
    public Block(int index, List<DecisionResult> decisions, String previousHash) {
        this(
            index,
            Collections.unmodifiableList(decisions),
            previousHash,
            System.currentTimeMillis(),
            calculateHash(index, decisions, previousHash, System.currentTimeMillis()),
            extractSrcIP(decisions),
            extractDestIP(decisions),
            extractSrcPort(decisions),
            extractDestPort(decisions),
            extractProtocol(decisions),
            extractPayload(decisions),
            extractSize(decisions),
            extractPacketTimestamp(decisions),
            extractAction(decisions)
        );
    }
    
    /**
     * Constructeur pour le bloc Genesis.
     */
    public static Block createGenesisBlock() {
        long timestamp = System.currentTimeMillis();
        return new Block(
            0,
            Collections.emptyList(),
            "0",
            timestamp,
            calculateHash(0, Collections.emptyList(), "0", timestamp),
            "0.0.0.0",
            "0.0.0.0",
            0,
            0,
            "GENESIS",
            "Genesis Block",
            0,
            LocalDateTime.now(),
            "NONE"
        );
    }

    /**
     * Calcule le hash SHA-256 du bloc.
     */
    private static String calculateHash(int index, List<DecisionResult> decisions, 
                                       String previousHash, long timestamp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder dataToHash = new StringBuilder();
            dataToHash.append(index)
                     .append(previousHash)
                     .append(timestamp);
            
            for (DecisionResult decision : decisions) {
                dataToHash.append(decision.toString());
            }
            
            byte[] hashBytes = digest.digest(dataToHash.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur de hachage SHA-256 : " + e.getMessage());
        }
    }
    
    // Méthodes d'extraction des informations du premier paquet
    private static String extractSrcIP(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? "0.0.0.0" : decisions.get(0).getPacket().getSrcIP();
    }
    
    private static String extractDestIP(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? "0.0.0.0" : decisions.get(0).getPacket().getDestIP();
    }
    
    private static int extractSrcPort(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? 0 : decisions.get(0).getPacket().getSrcPort();
    }
    
    private static int extractDestPort(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? 0 : decisions.get(0).getPacket().getDestPort();
    }
    
    private static String extractProtocol(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? "NONE" : decisions.get(0).getPacket().getProtocol();
    }
    
    private static String extractPayload(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? "" : decisions.get(0).getPacket().getPayload();
    }
    
    private static int extractSize(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? 0 : decisions.get(0).getPacket().getSize();
    }
    
    private static LocalDateTime extractPacketTimestamp(List<DecisionResult> decisions) {
        return decisions.isEmpty() ? LocalDateTime.now() : decisions.get(0).getPacket().getTimestamp();
    }

    /**
     *  Extrait l'action de la décision
     */
    private static String extractAction(List<DecisionResult> decisions) {
        if (decisions.isEmpty()) {
            return "NONE";
        }
        return decisions.get(0).getAction().toString();
    }

    /**
     * Extrait les actions des décisions pour affichage
     */
    public String getDecisionActions() {
        if (decisions.isEmpty()) {
            return "NONE";
        }
        
        return decisions.stream()
            .map(d -> d.getAction().toString())
            .reduce((a, b) -> a + ", " + b)
            .orElse("NONE");
    }

    @Override
    public String toString() {
        return String.format("Block #%d [%s:%d -> %s:%d | %s | action=%s | hash=%s, decisions=%d, timestamp=%d]",
            index, srcIP, srcPort, destIP, destPort, protocol, action,
            hash.substring(0, Math.min(10, hash.length())) + "...", 
            decisions.size(), timestamp);
    }
    
    /**
     Convertit le bloc en format CSV avec l'ACTION
     */
    public String toCSV() {
        return String.format("%d,%s,%s,%d,%d,%s,%d,%d,\"%s\",%s,%s,%s",
            index,
            srcIP,
            destIP,
            srcPort,
            destPort,
            protocol,
            size,
            timestamp,
            packetTimestamp.toString(),
            previousHash,
            hash,
            action
        );
    }
    
    /**
     *  En-tête CSV avec colonne ACTION
     */
    public static String getCSVHeader() {
        return "Index,Source IP,Destination IP,Source Port,Destination Port,Protocol,Size,Block Timestamp,Packet Timestamp,Previous Hash,Hash,Action";
    }
}