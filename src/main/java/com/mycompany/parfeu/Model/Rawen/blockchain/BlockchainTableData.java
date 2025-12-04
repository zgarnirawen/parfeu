package com.mycompany.parfeu.Model.Rawen.blockchain;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe de données pour l'affichage dans le TableView.
 * 🔥 TOUS LES GETTERS DOIVENT ÊTRE PUBLICS pour PropertyValueFactory
 */
public class BlockchainTableData {
    
    private final int index;
    private final String timestamp;
    private final String srcIP;
    private final String destIP;
    private final String protocol;
    private final int decisionsCount;
    private final String hashShort;
    private final String hashFull;

    public BlockchainTableData(Block block) {
        this.index = block.index();
        
        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.timestamp = sdf.format(new Date(block.timestamp()));
        
        this.srcIP = block.srcIP();
        this.destIP = block.destIP();
        this.protocol = block.protocol();
        this.decisionsCount = block.decisions().size();
        
        // Hash tronqué
        String hash = block.hash();
        this.hashFull = hash;
        this.hashShort = truncateHash(hash);
        
        // 🔥 DEBUG
        System.out.println("BlockchainTableData créé: #" + index + 
                         " | " + srcIP + " -> " + destIP + 
                         " | " + protocol);
    }

    private String truncateHash(String hash) {
        if (hash == null || hash.length() <= 16) {
            return hash;
        }
        return hash.substring(0, 8) + "..." + hash.substring(hash.length() - 8);
    }

    // 🔥 GETTERS PUBLICS (OBLIGATOIRE pour JavaFX PropertyValueFactory)
    public int getIndex() {
        return index;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public String getDestIP() {
        return destIP;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getDecisionsCount() {
        return decisionsCount;
    }

    public String getHashShort() {
        return hashShort;
    }

    public String getHashFull() {
        return hashFull;
    }

    @Override
    public String toString() {
        return "Block #" + index + " [" + timestamp + "] " + 
               srcIP + " -> " + destIP + " (" + protocol + ")";
    }
}