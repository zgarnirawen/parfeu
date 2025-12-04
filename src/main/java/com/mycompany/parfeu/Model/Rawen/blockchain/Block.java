package com.mycompany.parfeu.Model.Rawen.blockchain;

import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Block immuable avec informations complètes des paquets.
 * 🔥 VERSION CORRIGÉE : Support pour restauration depuis CSV sans recalcul de hash
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
    LocalDateTime packetTimestamp
) {

    /**
     * 🔥 NOUVEAU : Constructeur pour RESTAURATION depuis CSV
     * Accepte le hash existant SANS le recalculer
     */
    public Block(
        int index,
        List<DecisionResult> decisions,
        String previousHash,
        long timestamp,
        String hash,  // 🔥 Hash depuis CSV
        String srcIP,
        String destIP,
        int srcPort,
        int destPort,
        String protocol,
        String payload,
        int size,
        LocalDateTime packetTimestamp,
        boolean fromCSV  // 🔥 Flag pour différencier la restauration
    ) {
        this(
            index,
            Collections.unmodifiableList(decisions),
            previousHash,
            timestamp,
            hash,  // 🔥 UTILISER le hash fourni
            srcIP,
            destIP,
            srcPort,
            destPort,
            protocol,
            payload,
            size,
            packetTimestamp
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
            extractPacketTimestamp(decisions)
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
            LocalDateTime.now()
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
     * 🔥 NOUVELLE MÉTHODE : Extrait les actions des décisions pour affichage
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
        return String.format("Block #%d [%s:%d -> %s:%d | %s | hash=%s, decisions=%d, timestamp=%d]",
            index, srcIP, srcPort, destIP, destPort, protocol,
            hash.substring(0, Math.min(10, hash.length())) + "...", 
            decisions.size(), timestamp);
    }
    
    /**
     * Convertit le bloc en format CSV pour historique.
     */
    public String toCSV() {
        return String.format("%d,%s,%s,%d,%d,%s,%d,%d,%s,%s,%s",
            index,
            srcIP,
            destIP,
            srcPort,
            destPort,
            protocol,
            size,
            timestamp,
            packetTimestamp,
            previousHash,
            hash
        );
    }
    
    /**
     * En-tête CSV pour l'historique.
     */
    public static String getCSVHeader() {
        return "Index,Source IP,Destination IP,Source Port,Destination Port,Protocol,Size,Block Timestamp,Packet Timestamp,Previous Hash,Hash";
    }
}