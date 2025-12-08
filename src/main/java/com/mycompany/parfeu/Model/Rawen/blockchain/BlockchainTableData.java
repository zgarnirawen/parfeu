package com.mycompany.parfeu.Model.Rawen.blockchain;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe de données pour l'affichage dans le TableView.
 
 */
public class BlockchainTableData {
    
    private final int index;
    private final String timestamp;
    private final String srcIP;
    private final String destIP;
    private final String protocol;
    private final int decisionsCount;
    private final String decisions;  
    private final String hashShort;
    private final String hashFull;
    private final boolean isGenesis;  

    public BlockchainTableData(Block block) {
        this.index = block.index();
        
        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.timestamp = sdf.format(new Date(block.timestamp()));
        
        this.srcIP = block.srcIP();
        this.destIP = block.destIP();
        this.protocol = block.protocol();
        this.decisionsCount = block.decisions().size();
        
        //  Extraire les actions des décisions
        this.decisions = block.getDecisionActions();
        
        //  Détecter le Genesis
        this.isGenesis = block.index() == 0 && "0.0.0.0".equals(block.srcIP());
        
        // Hash tronqué
        String hash = block.hash();
        this.hashFull = hash;
        this.hashShort = truncateHash(hash);
        
        System.out.println("BlockchainTableData créé: #" + index + 
                         " | " + srcIP + " -> " + destIP + 
                         " | " + protocol +
                         " | Decisions: " + decisions);
    }

    private String truncateHash(String hash) {
        if (hash == null || hash.length() <= 16) {
            return hash;
        }
        return hash.substring(0, 8) + "..." + hash.substring(hash.length() - 8);
    }

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

    /**
       Retourne le texte des décisions pour affichage
     */
    public String getDecisions() {
        if (isGenesis) {
            return "GENESIS";
        }
        return decisions;
    }

    public String getHashShort() {
        return hashShort;
    }

    public String getHashFull() {
        return hashFull;
    }

    public boolean isGenesis() {
        return isGenesis;
    }

    @Override
    public String toString() {
        return "Block #" + index + " [" + timestamp + "] " + 
               srcIP + " -> " + destIP + " (" + protocol + ") - " + decisions;
    }
}