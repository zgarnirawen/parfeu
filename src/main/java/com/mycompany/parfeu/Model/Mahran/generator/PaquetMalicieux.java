/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Mahran.generator;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Paquet malicieux : ajoute un champ typeAttaque (ex: "DOS", "SCAN", "XSS", ...).
 */
public final class PaquetMalicieux extends Packet {
    private final String typeAttaque;

    public PaquetMalicieux(String srcIP,
                           String destIP,
                           int srcPort,
                           int destPort,
                           String protocol,
                           String payload,
                           String typeAttaque,
                           LocalDateTime timestamp) {
        super(srcIP, destIP, srcPort, destPort, protocol, payload, timestamp);
        this.typeAttaque = Objects.requireNonNull(typeAttaque, "typeAttaque ne peut pas être null");
    }

    public PaquetMalicieux(String srcIP,
                           String destIP,
                           int srcPort,
                           int destPort,
                           String protocol,
                           String payload,
                           String typeAttaque) {
        this(srcIP, destIP, srcPort, destPort, protocol, payload, typeAttaque, null);
    }

    public String getTypeAttaque() { return typeAttaque; }

    @Override
    public byte[] serialize() {
        String base = new String(super.serialize());
        String extended = "MALICIEUX|" + typeAttaque + "|" + base;
        return extended.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "PaquetMalicieux{" +
                "typeAttaque='" + typeAttaque + '\'' +
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
}


