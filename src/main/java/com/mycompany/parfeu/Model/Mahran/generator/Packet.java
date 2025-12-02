/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.generator;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe abstraite représentant un paquet réseau.
 * Implémente IPaquet (interface fonctionnelle).
 * Override toutes les méthodes default de l'interface.
 * 
 * @author ZGARNI
 */
public sealed abstract class Packet implements IPaquet permits PaquetSimple, PaquetMalicieux {
    protected final String srcIP;
    protected final String destIP;
    protected final int srcPort;
    protected final int destPort;
    protected final String protocol;
    protected final String payload;
    protected final int size;
    protected final LocalDateTime timestamp;

    protected Packet(String srcIP, String destIP, int srcPort, int destPort, 
                     String protocol, String payload, LocalDateTime timestamp) {
        this.srcIP = Objects.requireNonNull(srcIP, "srcIP ne peut pas être null");
        this.destIP = Objects.requireNonNull(destIP, "destIP ne peut pas être null");
        this.srcPort = validatePort(srcPort);
        this.destPort = validatePort(destPort);
        this.protocol = Objects.requireNonNull(protocol, "protocol ne peut pas être null");
        this.payload = payload == null ? "" : payload;
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
        this.size = computeSize();
    }

    private static int validatePort(int port) {
        if (port < 0 || port > 65535) 
            throw new IllegalArgumentException("Port invalide: " + port);
        return port;
    }

    protected int computeSize() {
        int headerEstimate = 20;
        int payloadBytes = payload.getBytes(StandardCharsets.UTF_8).length;
        return headerEstimate + payloadBytes;
    }

    // ========== OVERRIDE DES MÉTHODES DEFAULT DE L'INTERFACE ==========
    
    @Override
    public String getSrcIP() { 
        return srcIP; 
    }

    @Override
    public String getDestIP() { 
        return destIP; 
    }

    @Override
    public int getSrcPort() { 
        return srcPort; 
    }

    @Override
    public int getDestPort() { 
        return destPort; 
    }

    @Override
    public String getProtocol() { 
        return protocol; 
    }

    @Override
    public String getPayload() { 
        return payload; 
    }

    @Override
    public int getSize() { 
        return size; 
    }

    @Override
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }

    /**
     * IMPLÉMENTATION DE LA MÉTHODE ABSTRAITE (serialize).
     */
    @Override
    public byte[] serialize() {
        String s = this.getClass().getSimpleName() + "|" +
                   srcIP + "->" + destIP + "|" +
                   srcPort + "->" + destPort + "|" +
                   protocol + "|" +
                   timestamp + "|" +
                   payload;
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String summary() {
        return String.format("%s %s:%d -> %s:%d [%s] size=%d payload=%dbytes",
                timestamp,
                srcIP, srcPort,
                destIP, destPort,
                protocol,
                size,
                payload.getBytes(StandardCharsets.UTF_8).length);
    }

    @Override
    public String toString() {
        return "Packet{" +
                "type=" + this.getClass().getSimpleName() +
                ", srcIP='" + srcIP + '\'' +
                ", destIP='" + destIP + '\'' +
                ", srcPort=" + srcPort +
                ", destPort=" + destPort +
                ", protocol='" + protocol + '\'' +
                ", payload='" + payload + '\'' +
                ", size=" + size +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Packet packet = (Packet) o;
        return srcPort == packet.srcPort &&
                destPort == packet.destPort &&
                Objects.equals(srcIP, packet.srcIP) &&
                Objects.equals(destIP, packet.destIP) &&
                Objects.equals(protocol, packet.protocol) &&
                Objects.equals(payload, packet.payload) &&
                Objects.equals(timestamp, packet.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcIP, destIP, srcPort, destPort, protocol, payload, timestamp);
    }
}