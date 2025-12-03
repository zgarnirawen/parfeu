package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionEngine;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Rawen.persistence.StorageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la décision finale du pare-feu.
 */
public class FinalDecisionController implements Initializable {

    @FXML private Label actionLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label signalsCountLabel;
    @FXML private Label riskLevelSummaryLabel;
    @FXML private Label timestampLabel;
    @FXML private TextArea reasonArea;
    @FXML private TextArea completeReportArea;
    @FXML private Button backBtn;
    @FXML private Button saveToBlockchainBtn;

    private Packet currentPacket;
    private List<DetectionSignal> signals;
    private DecisionResult decision;
    private DecisionEngine decisionEngine;
    private StorageManager storageManager;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            FirewallConfig config = new FirewallConfig();
            decisionEngine = new DecisionEngine(config);
            storageManager = new StorageManager();
            
            setupButtons();
            saveToBlockchainBtn.setDisable(true);
            
            System.out.println("✓ FinalDecisionController initialized");
        } catch (Exception e) {
            showError("Initialization Error", "Failed to initialize controller: " + e.getMessage());
        }
    }

    /**
     * Définit les données de décision.
     */
    public void setDecisionData(Packet packet, List<DetectionSignal> signals) {
        this.currentPacket = packet;
        this.signals = signals;
        makeDecision();
    }

    /**
     * Prend la décision finale.
     */
    private void makeDecision() {
        if (currentPacket == null || signals == null) {
            showWarning("No Data", "Please complete the analysis first");
            return;
        }

        try {
            // Décision finale
            decision = decisionEngine.decide(currentPacket, signals);
            
            // Mise à jour de l'interface
            displayDecision();
            displaySummary();
            displayReason();
            displayCompleteReport();
            
            // Activer le bouton de sauvegarde
            saveToBlockchainBtn.setDisable(false);

        } catch (Exception e) {
            showError("Decision Error", "Failed to make decision: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche la décision.
     */
    private void displayDecision() {
        String action = decision.getAction().toString();
        actionLabel.setText(action);
        
        // Style selon l'action
        String style = switch (action) {
            case "ACCEPT" -> "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 15 30 15 30; -fx-background-radius: 12;";
            case "DROP" -> "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 15 30 15 30; -fx-background-radius: 12;";
            case "ALERT" -> "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 15 30 15 30; -fx-background-radius: 12;";
            case "LOG" -> "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 15 30 15 30; -fx-background-radius: 12;";
            default -> "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 15 30 15 30; -fx-background-radius: 12;";
        };
        
        actionLabel.setStyle(style);
    }

    /**
     * Affiche le résumé de la décision.
     */
    private void displaySummary() {
        finalScoreLabel.setText(decision.getTotalScore() + " / 10");
        signalsCountLabel.setText(String.valueOf(signals.size()));
        
        String riskLevel = decisionEngine.evaluateRiskLevel(decision.getTotalScore());
        riskLevelSummaryLabel.setText(riskLevel);
        
        // Couleur selon le risque
        String color = switch (riskLevel) {
            case "SAFE" -> "-fx-text-fill: #27ae60;";
            case "LOW" -> "-fx-text-fill: #3498db;";
            case "MEDIUM" -> "-fx-text-fill: #f39c12;";
            case "HIGH" -> "-fx-text-fill: #e67e22;";
            case "CRITICAL" -> "-fx-text-fill: #e74c3c;";
            default -> "-fx-text-fill: #95a5a6;";
        };
        
        riskLevelSummaryLabel.setStyle(color + " -fx-font-weight: bold;");
        
        timestampLabel.setText(decision.getTimestamp().format(formatter));
    }

    /**
     * Affiche la raison de la décision.
     */
    private void displayReason() {
        reasonArea.setText(decision.getReason());
    }

    /**
     * Affiche le rapport complet.
     */
    private void displayCompleteReport() {
        completeReportArea.setText(decision.getDetailedSummary());
    }

    /**
     * Sauvegarde dans la blockchain.
     */
    private void saveToBlockchain() {
        if (decision == null) {
            showWarning("No Decision", "Please complete the analysis first");
            return;
        }

        try {
            // Créer un nouveau bloc
            Block newBlock = new Block(
                getNextBlockIndex(),
                List.of(decision),
                getLastBlockHash()
            );

            // Sauvegarder dans l'historique
            storageManager.saveBlockToHistory(newBlock);

            showSuccess("Saved Successfully", 
                "Decision saved to blockchain!\n\n" +
                "Block #" + newBlock.index() + " created\n" +
                "Hash: " + truncateHash(newBlock.hash()));

            // Retourner au menu
            try {
                Thread.sleep(1000); // Petit délai pour voir le message
                App.loadMainMenu();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } catch (Exception e) {
            showError("Save Error", "Failed to save to blockchain: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure les boutons.
     */
    private void setupButtons() {
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadScene("/com/mycompany/parfeu/Views/Rawen/deep_analysis.fxml", 900, 800);
                } catch (IOException e) {
                    showError("Navigation Error", "Cannot return to previous page");
                }
            });
        }

        if (saveToBlockchainBtn != null) {
            saveToBlockchainBtn.setOnAction(event -> saveToBlockchain());
        }
    }

    private int getNextBlockIndex() {
        try {
            return (int) storageManager.countBlocks() + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private String getLastBlockHash() {
        try {
            List<String> history = storageManager.loadBlockHistory();
            if (history.isEmpty()) {
                return "0"; // Genesis
            }
            String lastLine = history.get(history.size() - 1);
            String[] parts = lastLine.split(",");
            return parts[parts.length - 1]; // Last field = hash
        } catch (Exception e) {
            return "0";
        }
    }

    private String truncateHash(String hash) {
        if (hash == null || hash.length() <= 16) return hash;
        return hash.substring(0, 8) + "..." + hash.substring(hash.length() - 8);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getters
    public DecisionResult getDecision() { return decision; }
}