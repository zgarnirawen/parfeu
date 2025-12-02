/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parfeu.Model.Rawen.blockchain;

import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

/**
 * Represente un bloc immuable dans la blockchain.
 * Contient une liste de decisions du pare-feu.
 * 
 * Concept Java : Record (Java 16+)
 * 
 * @author ZGARNI
 */
public record Block(
    int index,
    List<DecisionResult> decisions,
    String previousHash,
    long timestamp,
    String hash
) {

    /**
     * Constructeur pour creer un nouveau bloc.
     * @param index numero du bloc
     * @param decisions liste des decisions a stocker
     * @param previousHash hash du bloc precedent
     */
    public Block(int index, List<DecisionResult> decisions, String previousHash) {
        this(
            index,
            Collections.unmodifiableList(decisions),
            previousHash,
            System.currentTimeMillis(),
            calculateHash(index, decisions, previousHash, System.currentTimeMillis())
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

    @Override
    public String toString() {
        return String.format("Block #%d [hash=%s, decisions=%d, timestamp=%d]",
            index, hash.substring(0, 10) + "...", decisions.size(), timestamp);
    }
}