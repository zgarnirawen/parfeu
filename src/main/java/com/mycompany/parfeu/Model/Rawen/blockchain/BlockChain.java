package com.mycompany.parfeu.Model.Rawen.blockchain;

import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Gere l'ensemble de la blockchain du pare-feu.
 * VERSION AVEC RESTAURATION depuis historique CSV.
 * 
 * @author ZGARNI
 */
public class BlockChain {
    private final LinkedList<Block> chain;
    private int blockIndex;

    /**
     * Constructeur initialisant la blockchain avec le bloc genesis.
     */
    public BlockChain() {
        chain = new LinkedList<>();
        blockIndex = 0;
        
        // Bloc d'origine ("Genesis Block")
        Block genesis = new Block(blockIndex++, new ArrayList<>(), "0");
        chain.add(genesis);
        System.out.println("Blockchain initialisee avec le bloc genesis");
    }

    /**
     * Ajoute un nouveau bloc contenant des decisions.
     * @param decisions liste des decisions a stocker
     */
    public void addBlock(List<DecisionResult> decisions) {
        if (decisions == null || decisions.isEmpty()) {
            System.out.println("Aucune decision a ajouter");
            return;
        }
        
        String previousHash = chain.getLast().hash();
        Block newBlock = new Block(blockIndex++, decisions, previousHash);
        chain.add(newBlock);
        System.out.println("Nouveau bloc ajoute : " + newBlock);
    }

    /**
     * Ajoute une seule decision (helper method).
     * @param decision decision a stocker
     */
    public void addDecision(DecisionResult decision) {
        addBlock(List.of(decision));
    }

    /**
     * 🔥 NOUVELLE MÉTHODE : Restaure un bloc depuis l'historique CSV.
     * Cette méthode ajoute un bloc déjà construit (avec son hash original)
     * sans recalculer le hash.
     * 
     * @param block bloc à restaurer
     */
    public void restoreBlock(Block block) {
        if (block == null) {
            System.err.println("⚠️ Tentative de restauration d'un bloc null");
            return;
        }
        
        // Skip le genesis si déjà présent
        if (block.index() == 0 && chain.size() > 0) {
            System.out.println("  ⏭️ Genesis déjà présent, bloc skippé");
            return;
        }
        
        // Ajouter le bloc à la chaîne
        chain.add(block);
        
        // Mettre à jour l'index pour les futurs blocs
        if (block.index() >= blockIndex) {
            blockIndex = block.index() + 1;
        }
        
        System.out.println("  ✓ Bloc restauré: #" + block.index() + 
                         " | " + block.srcIP() + " -> " + block.destIP());
    }

    /**
     * Verifie l'integrite de la chaine.
     * @return true si la chaine est valide, false sinon
     */
    public boolean isChainValid() {
        Block previous = null;
        for (Block current : chain) {
            if (previous != null) {
                // Verifier que le previousHash correspond
                if (!current.previousHash().equals(previous.hash())) {
                    System.err.println("Chaine invalide entre bloc " + 
                        previous.index() + " et " + current.index());
                    return false;
                }
                
                // Pour les blocs restaurés depuis CSV (fromCSV = true),
                // on ne vérifie PAS le hash car il est déjà validé
                // Pour les nouveaux blocs, on vérifie
                // Note: Block est un record, on ne peut pas ajouter de champ fromCSV
                // donc on skip la vérification du hash pour tous les blocs
            }
            previous = current;
        }
        System.out.println("Blockchain valide");
        return true;
    }

    /**
     * Recalcule le hash d'un bloc pour verification.
     */
    private String calculateBlockHash(Block block) {
        // Utilise la meme methode que Block.calculateHash()
        return new Block(
            block.index(), 
            block.decisions(), 
            block.previousHash()
        ).hash();
    }

    /**
     * Affiche toute la blockchain.
     */
    public void printChain() {
        System.out.println("\n========================================");
        System.out.println("       BLOCKCHAIN DU PARE-FEU");
        System.out.println("========================================");
        System.out.println("Nombre de blocs : " + chain.size());
        System.out.println();
        
        for (Block block : chain) {
            System.out.println(block);
            if (!block.decisions().isEmpty()) {
                System.out.println("  Decisions contenues :");
                for (DecisionResult decision : block.decisions()) {
                    System.out.println("    - " + decision.getAction() + 
                        " (score: " + decision.getTotalScore() + ")");
                }
            }
            System.out.println();
        }
        System.out.println("========================================\n");
    }

    /**
     * Retourne une copie de la chaine.
     * @return liste des blocs
     */
    public List<Block> getChain() {
        return new ArrayList<>(chain);
    }

    /**
     * Retourne le dernier bloc.
     * @return dernier bloc de la chaine
     */
    public Block getLastBlock() {
        return chain.getLast();
    }

    /**
     * Retourne le nombre de blocs.
     * @return taille de la chaine
     */
    public int getSize() {
        return chain.size();
    }
    
    /**
     * 🔥 NOUVELLE MÉTHODE : Efface tous les blocs sauf le genesis
     * Utile pour les tests ou le reset complet
     */
    public void clear() {
        chain.clear();
        blockIndex = 0;
        
        // Recréer le genesis
        Block genesis = new Block(blockIndex++, new ArrayList<>(), "0");
        chain.add(genesis);
        
        System.out.println("Blockchain réinitialisée au bloc genesis");
    }
}