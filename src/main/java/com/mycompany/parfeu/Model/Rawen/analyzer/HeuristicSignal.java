/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.analyzer;

/**
 *
 * @author ZGARNI
 */
import com.mycompany.parfeu.Model.Mahran.generator.Packet;

/**
 * Signal heuristique détectant des anomalies comportementales.
 * @author ZGARNI
 */
public final class HeuristicSignal implements DetectionSignal {
    private final int score;
    private final String description;

    // Constructeur privé
    private HeuristicSignal(int score, String description) {
        this.score = score;
        this.description = description;
    }

    /**
     * Analyse heuristique d'un paquet.
     * Détecte : paquets volumineux, ports privilégiés
     */
    public static HeuristicSignal analyze(Packet packet) {
        int heuristicScore = 0;
        StringBuilder desc = new StringBuilder();
        
        if (packet.getSize() > 1500) {
            heuristicScore += 1;
            desc.append("Paquet volumineux. ");
        }
        if (packet.getDestPort() < 1024) {
            heuristicScore += 1;
            desc.append("Port privilégié. ");
        }
        
        if (heuristicScore > 0) {
            return new HeuristicSignal(heuristicScore, desc.toString());
        }
        return null;
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public String getDescription() {
        return description;
    }
}