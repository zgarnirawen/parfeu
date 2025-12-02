/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.analyzer;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;

/**
 Signal de détection basé sur la taille du paquet.
 * Implémente DetectionSignal (interface fonctionnelle).
 * 
 * @author ZGARNI
 */
public final class SizeSignal implements DetectionSignal {
    private final int size;
    private final int score;

    private SizeSignal(int size, int score) {
        this.size = size;
        this.score = score;
    }

    public static SizeSignal analyze(Packet packet, int min, int max) {
        if (packet.getSize() < min || packet.getSize() > max) {
            return new SizeSignal(packet.getSize(), 1);
        }
        return null;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int getScore() {
        return score;
    }

    /**
     * Override de la méthode default pour fournir une description spécifique.
     */
    @Override
    public String getDescription() {
        return "Taille anormale détectée: " + size + " bytes";
    }
}