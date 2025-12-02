package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockChain;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la vue Blockchain (historique).
 */
public class BlockchainController implements Initializable {

    @FXML
    private VBox blocksContainer;
    
    @FXML
    private HBox inforow;
    
    @FXML
    private Button backBtn;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button verifyBtn;

    private BlockChain blockchain;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser la blockchain
        blockchain = new BlockChain();
        
        // Ajouter des données de test
        addTestData();
        
        // Charger et afficher la blockchain
        loadBlockchain();
        
        // Configurer les boutons
        setupButtons();
        
        System.out.println("✓ BlockchainController initialisé");
    }

    /**
     * Ajoute des données de test à la blockchain.
     */
    private void addTestData() {
        // Simuler quelques décisions pour tester
        // Dans une vraie application, ces données viendraient du FirewallEngine
        System.out.println("Ajout de données de test à la blockchain...");
        
        // Note: Vous devrez créer des DecisionResult réels ici
        // Pour l'instant, on initialise juste avec le bloc genesis
    }

    /**
     * Charge et affiche tous les blocs de la blockchain.
     */
    private void loadBlockchain() {
        blocksContainer.getChildren().clear();
        
        // Mettre à jour les infos générales
        updateInfoRow();
        
        // Charger chaque bloc
        for (Block block : blockchain.getChain()) {
            try {
                VBox blockCard = createBlockCard(block);
                blocksContainer.getChildren().add(blockCard);
            } catch (Exception e) {
                System.err.println("Erreur lors de la création de la carte pour le bloc " + block.index());
                e.printStackTrace();
            }
        }
    }

    /**
     * Crée une carte visuelle pour un bloc.
     */
    private VBox createBlockCard(Block block) throws IOException {
        // Charger le template FXML de la carte
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/mycompany/parfeu/Views/Rawen/block_card.fxml")
        );
        VBox blockCard = loader.load();
        
        // Obtenir le contrôleur et remplir les données
        Block_cardController controller = loader.getController();
        controller.setBlockData(block);
        
        return blockCard;
    }

    /**
     * Met à jour la ligne d'informations générales.
     */
    private void updateInfoRow() {
        if (inforow != null && inforow.getChildren().size() >= 3) {
            // Mettre à jour le nombre de blocs
            Label blocksLabel = (Label) inforow.getChildren().get(0);
            blocksLabel.setText("Blocks: " + blockchain.getSize());
            
            // Mettre à jour le statut
            Label statusLabel = (Label) inforow.getChildren().get(1);
            boolean isValid = blockchain.isChainValid();
            statusLabel.setText("Status: " + (isValid ? "✓ Valid" : "✗ Invalid"));
            statusLabel.setStyle(isValid ? 
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : 
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            
            // Mettre à jour le dernier bloc
            Label lastBlockLabel = (Label) inforow.getChildren().get(2);
            Block lastBlock = blockchain.getLastBlock();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(lastBlock.timestamp()));
            lastBlockLabel.setText("Last Block: " + timestamp);
        }
    }

    /**
     * Configure les actions des boutons.
     */
    private void setupButtons() {
        // Bouton Retour
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadScene("/com/mycompany/parfeu/Views/Mahran/mainvue.fxml", 800, 600);
                } catch (IOException e) {
                    showError("Erreur", "Impossible de retourner au menu principal");
                    e.printStackTrace();
                }
            });
        }
        
        // Bouton Rafraîchir
        if (refreshBtn != null) {
            refreshBtn.setOnAction(event -> {
                loadBlockchain();
                showInfo("Rafraîchi", "La blockchain a été rechargée");
            });
        }
        
        // Bouton Vérifier
        if (verifyBtn != null) {
            verifyBtn.setOnAction(event -> {
                boolean isValid = blockchain.isChainValid();
                if (isValid) {
                    showInfo("✓ Blockchain Valide", 
                        "L'intégrité de la blockchain a été vérifiée avec succès.\n" +
                        "Tous les blocs sont valides et connectés correctement.");
                } else {
                    showError("✗ Blockchain Invalide", 
                        "ATTENTION: La blockchain a été compromise!\n" +
                        "Un ou plusieurs blocs ont été modifiés.");
                }
                updateInfoRow();
            });
        }
    }

    /**
     * Ajoute une nouvelle décision à la blockchain.
     */
    public void addDecisionToBlockchain(DecisionResult decision) {
        blockchain.addDecision(decision);
        loadBlockchain();
    }

    /**
     * Affiche une alerte d'information.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getters
    public BlockChain getBlockchain() {
        return blockchain;
    }
}