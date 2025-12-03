package com.mycompany.parfeu.Model.Mahran.generator;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Record pour l'input utilisateur d'un paquet.
 * Permet de créer un paquet de façon guidée depuis l'interface.
 * 
 * @author ZGARNI
 */
public record PacketInput(
    String srcIP,
    String destIP,
    int srcPort,
    int destPort,
    String protocol,
    String payload,
    boolean isMalicious,
    String attackType  // null si paquet sain
) {
    
    /**
     * Constructeur compact avec validation.
     */
    public PacketInput {
        Objects.requireNonNull(srcIP, "srcIP ne peut pas être null");
        Objects.requireNonNull(destIP, "destIP ne peut pas être null");
        Objects.requireNonNull(protocol, "protocol ne peut pas être null");
        
        if (srcPort < 0 || srcPort > 65535) {
            throw new IllegalArgumentException("srcPort invalide: " + srcPort);
        }
        if (destPort < 0 || destPort > 65535) {
            throw new IllegalArgumentException("destPort invalide: " + destPort);
        }
        
        // Validation IP
        if (!isValidIP(srcIP)) {
            throw new IllegalArgumentException("srcIP invalide: " + srcIP);
        }
        if (!isValidIP(destIP)) {
            throw new IllegalArgumentException("destIP invalide: " + destIP);
        }
        
        // Si malicieux, attackType doit être défini
        if (isMalicious && (attackType == null || attackType.trim().isEmpty())) {
            throw new IllegalArgumentException("attackType requis pour un paquet malicieux");
        }
        
        payload = payload == null ? "" : payload;
    }
    
    /**
     * Constructeur pour paquet sain.
     */
    public PacketInput(String srcIP, String destIP, int srcPort, int destPort, 
                      String protocol, String payload) {
        this(srcIP, destIP, srcPort, destPort, protocol, payload, false, null);
    }
    
    /**
     * Convertit ce PacketInput en Packet concret.
     * @return PaquetSimple ou PaquetMalicieux selon isMalicious
     */
    public Packet toPacket() {
        if (isMalicious) {
            return new PaquetMalicieux(
                srcIP, destIP, srcPort, destPort, 
                protocol, payload, attackType, LocalDateTime.now()
            );
        } else {
            return new PaquetSimple(
                srcIP, destIP, srcPort, destPort, 
                protocol, payload, LocalDateTime.now()
            );
        }
    }
    
    /**
     * Valide une adresse IP.
     */
    private static boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Crée un builder pour faciliter la création.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder pour PacketInput.
     */
    public static class Builder {
        private String srcIP;
        private String destIP;
        private int srcPort;
        private int destPort;
        private String protocol;
        private String payload = "";
        private boolean isMalicious = false;
        private String attackType;
        
        public Builder srcIP(String srcIP) {
            this.srcIP = srcIP;
            return this;
        }
        
        public Builder destIP(String destIP) {
            this.destIP = destIP;
            return this;
        }
        
        public Builder srcPort(int srcPort) {
            this.srcPort = srcPort;
            return this;
        }
        
        public Builder destPort(int destPort) {
            this.destPort = destPort;
            return this;
        }
        
        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }
        
        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }
        
        public Builder malicious(boolean isMalicious) {
            this.isMalicious = isMalicious;
            return this;
        }
        
        public Builder attackType(String attackType) {
            this.attackType = attackType;
            this.isMalicious = true;
            return this;
        }
        
        public PacketInput build() {
            return new PacketInput(
                srcIP, destIP, srcPort, destPort, 
                protocol, payload, isMalicious, attackType
            );
        }
    }
}