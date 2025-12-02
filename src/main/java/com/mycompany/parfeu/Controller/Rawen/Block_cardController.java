package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la carte d'un bloc de la blockchain.
 */
public class Block_cardController implements Initializable {

    @FXML
    private VBox blockCardRoot;
    
    @FXML
    private Label lblBlockIndex;
    
    @FXML
    private Label indexbloc;
    
    @FXML
    private VBox decisionsBox;
    
    @FXML
    private Label lblPrevHash;
    
    @FXML
    private Label lblTimestamp;
    
    @FXML
    private Label lblHash;

    private Block block;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // L'initialisation sera faite par setBlockData()
    }

    /**
     * Définit les données du bloc à afficher.
     */
    public void setBlockData(Block block) {
        this.block = block;
        displayBlockInfo();
    }

    /**
     * Affiche toutes les informations du bloc.
     */
    private void displayBlockInfo() {
        if (block == null) {
            System.err.println("⚠ Bloc null dans Block_cardController");
            return;
        }

        // Index du bloc
        if (lblBlockIndex != null) {
            lblBlockIndex.setText("Block #" + block.index());
        }
        if (indexbloc != null) {
            indexbloc.setText(String.valueOf(block.index()));
        }

        // Hash précédent (tronqué pour l'affichage)
        if (lblPrevHash != null) {
            String prevHash = block.previousHash();
            if (prevHash.equals("0")) {
                lblPrevHash.setText("0 (Genesis Block)");
                lblPrevHash.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else {
                lblPrevHash.setText(truncateHash(prevHash));
            }
        }

        // Timestamp
        if (lblTimestamp != null) {
            String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(block.timestamp()));
            lblTimestamp.setText(formattedTime);
        }

        // Hash du bloc (tronqué)
        if (lblHash != null) {
            lblHash.setText(truncateHash(block.hash()));
        }

        // Décisions contenues dans le bloc
        if (decisionsBox != null) {
            decisionsBox.getChildren().clear();
            
            if (block.decisions().isEmpty()) {
                Label noDecisions = new Label("(Genesis Block - No decisions)");
                noDecisions.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                decisionsBox.getChildren().add(noDecisions);
            } else {
                Label decisionsTitle = new Label("Decisions (" + block.decisions().size() + "):");
                decisionsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                decisionsBox.getChildren().add(decisionsTitle);
                
                for (int i = 0; i < block.decisions().size(); i++) {
                    DecisionResult decision = block.decisions().get(i);
                    VBox decisionCard = createDecisionCard(i + 1, decision);
                    decisionsBox.getChildren().add(decisionCard);
                }
            }
        }

        // Styliser la carte selon le type de bloc
        styleBlockCard();
    }

    /**
     * Crée une carte pour une décision.
     */
    private VBox createDecisionCard(int number, DecisionResult decision) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; " +
                     "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 8;");
        card.setPadding(new Insets(8));

        // Numéro et action
        HBox header = new HBox(10);
        Label numLabel = new Label(number + ".");
        numLabel.setStyle("-fx-font-weight: bold;");
        
        Label actionLabel = new Label(decision.getAction().toString());
        actionLabel.setStyle(getActionStyle(decision.getAction().toString()));
        
        header.getChildren().addAll(numLabel, actionLabel);

        // Score
        Label scoreLabel = new Label("Score: " + decision.getTotalScore() + "/10");
        scoreLabel.setStyle("-fx-text-fill: " + getScoreColor(decision.getTotalScore()) + ";");

        // IP source et destination
        Label ipLabel = new Label(
            decision.getPacket().getSrcIP() + " → " + 
            decision.getPacket().getDestIP()
        );
        ipLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6c757d;");

        card.getChildren().addAll(header, scoreLabel, ipLabel);
        return card;
    }

    /**
     * Retourne le style CSS pour une action.
     */
    private String getActionStyle(String action) {
        return switch (action) {
            case "ACCEPT" -> "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
            case "DROP" -> "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
            case "ALERT" -> "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
            case "LOG" -> "-fx-text-fill: #3498db; -fx-font-weight: bold;";
            default -> "-fx-text-fill: #7f8c8d;";
        };
    }

    /**
     * Retourne une couleur selon le score.
     */
    private String getScoreColor(int score) {
        if (score == 0) return "#27ae60";
        if (score <= 2) return "#3498db";
        if (score <= 4) return "#f39c12";
        if (score <= 7) return "#e67e22";
        return "#e74c3c";
    }

    /**
     * Tronque un hash pour l'affichage.
     */
    private String truncateHash(String hash) {
        if (hash == null || hash.length() <= 16) {
            return hash;
        }
        return hash.substring(0, 8) + "..." + hash.substring(hash.length() - 8);
    }

    /**
     * Stylise la carte selon le type de bloc.
     */
    private void styleBlockCard() {
        if (blockCardRoot == null) return;
        
        if (block.index() == 0) {
            // Genesis block - style spécial
            blockCardRoot.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #d4edda, #c3e6cb); " +
                "-fx-border-color: #28a745; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(40,167,69,0.3), 10, 0, 0, 2);"
            );
        } else if (block.decisions().isEmpty()) {
            // Bloc vide
            blockCardRoot.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-border-color: #dee2e6; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 16;"
            );
        } else {
            // Bloc normal avec décisions
            blockCardRoot.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e3e8ff, #d4d9f5); " +
                "-fx-border-color: #3a54a1; " +
                "-fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; " +
                "-fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(58,84,161,0.2), 10, 0, 0, 2);"
            );
        }
    }

    // Getters
    public Block getBlock() {
        return block;
    }
}