package com.mycompany.parfeu.Model.Rawen.blockchain;

import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 🔥 VERSION FINALE - Gère correctement le Genesis restauré
 */
public class BlockChain {
    private final LinkedList<Block> chain;
    private int blockIndex;
    private boolean isRestoring = false;  // 🔥 NOUVEAU : Flag de restauration

    public BlockChain() {
        chain = new LinkedList<>();
        blockIndex = 0;
        
        // 🔥 NE PAS créer le Genesis maintenant
        // Il sera créé lors de la première restauration OU lors du premier ajout
    }

    /**
     * Ajoute un nouveau bloc contenant des décisions.
     */
    public void addBlock(List<DecisionResult> decisions) {
        if (decisions == null || decisions.isEmpty()) {
            System.out.println("Aucune décision à ajouter");
            return;
        }
        
        // 🔥 Créer le Genesis si la chaîne est vide
        if (chain.isEmpty()) {
            Block genesis = new Block(blockIndex++, new ArrayList<>(), "0");
            chain.add(genesis);
            System.out.println("✓ Genesis créé : " + genesis.hash());
        }
        
        String previousHash = chain.getLast().hash();
        Block newBlock = new Block(blockIndex++, decisions, previousHash);
        chain.add(newBlock);
        System.out.println("✓ Nouveau bloc ajouté : #" + newBlock.index());
    }

    /**
     * Ajoute une seule décision (helper method).
     */
    public void addDecision(DecisionResult decision) {
        addBlock(List.of(decision));
    }

    /**
     * 🔥 Démarre la restauration depuis CSV
     */
    public void startRestoration() {
        isRestoring = true;
        chain.clear();  // Vider complètement la chaîne
        blockIndex = 0;
        System.out.println("🔄 Mode restauration activé");
    }

    /**
     * 🔥 Termine la restauration
     */
    public void finishRestoration() {
        isRestoring = false;
        System.out.println("✅ Mode restauration terminé");
        
        // Si aucun bloc restauré, créer le Genesis
        if (chain.isEmpty()) {
            Block genesis = new Block(blockIndex++, new ArrayList<>(), "0");
            chain.add(genesis);
            System.out.println("✓ Genesis créé (aucun bloc restauré)");
        }
    }

    /**
     * 🔥 RESTAURE un bloc depuis l'historique CSV.
     */
    public void restoreBlock(Block block) {
        if (block == null) {
            System.err.println("⚠️ Tentative de restauration d'un bloc null");
            return;
        }
        
        // 🔥 Ajouter TOUS les blocs, y compris le Genesis
        chain.add(block);
        
        // Mettre à jour l'index
        if (block.index() >= blockIndex) {
            blockIndex = block.index() + 1;
        }
        
        // Afficher les détails
        if (block.index() == 0) {
            System.out.println("  ✓ Genesis restauré: hash=" + 
                             block.hash().substring(0, 16) + "...");
        } else {
            System.out.println("  ✓ Bloc #" + block.index() + " restauré | " +
                             block.srcIP() + " -> " + block.destIP() + 
                             " | Action: " + block.action());
        }
    }

    /**
     * 🔥 Vérifie l'intégrité de la chaîne
     */
    public boolean isChainValid() {
        if (chain.isEmpty()) {
            System.out.println("⚠️ Blockchain vide");
            return true;
        }
        
        Block previous = null;
        
        for (Block current : chain) {
            if (previous != null) {
                // Vérifier que previousHash correspond
                if (!current.previousHash().equals(previous.hash())) {
                    System.err.println("❌ Chaîne invalide entre bloc #" + 
                        previous.index() + " et #" + current.index());
                    System.err.println("   Bloc #" + previous.index() + " hash    : " + previous.hash());
                    System.err.println("   Bloc #" + current.index() + " prevHash: " + current.previousHash());
                    System.err.println("   🔍 Les hash ne correspondent pas !");
                    return false;
                }
            }
            previous = current;
        }
        
        System.out.println("✅ Blockchain valide - " + chain.size() + " blocs vérifiés");
        return true;
    }

    /**
     * Affiche toute la blockchain avec détails.
     */
    public void printChain() {
        System.out.println("\n========================================");
        System.out.println("       BLOCKCHAIN DU PARE-FEU");
        System.out.println("========================================");
        System.out.println("Nombre de blocs : " + chain.size());
        System.out.println();
        
        for (Block block : chain) {
            System.out.println("Bloc #" + block.index());
            System.out.println("  Hash     : " + block.hash().substring(0, 32) + "...");
            System.out.println("  PrevHash : " + block.previousHash());
            System.out.println("  Action   : " + block.action());
            
            if (!block.decisions().isEmpty()) {
                for (DecisionResult decision : block.decisions()) {
                    System.out.println("  Decision : " + decision.getAction() + 
                        " (score: " + decision.getTotalScore() + ")");
                }
            }
            System.out.println();
        }
        System.out.println("========================================\n");
    }

    /**
     * Retourne une copie de la chaîne.
     */
    public List<Block> getChain() {
        return new ArrayList<>(chain);
    }

    /**
     * Retourne le dernier bloc.
     */
    public Block getLastBlock() {
        if (chain.isEmpty()) {
            // Créer le Genesis si nécessaire
            Block genesis = new Block(blockIndex++, new ArrayList<>(), "0");
            chain.add(genesis);
        }
        return chain.getLast();
    }

    /**
     * Retourne le nombre de blocs.
     */
    public int getSize() {
        return chain.size();
    }
    
    /**
     * Efface tous les blocs sauf le genesis.
     */
    public void clear() {
        chain.clear();
        blockIndex = 0;
        
        // Recréer le genesis
        Block genesis = new Block(blockIndex++, new ArrayList<>(), "0");
        chain.add(genesis);
        
        System.out.println("✓ Blockchain réinitialisée avec Genesis");
    }
}