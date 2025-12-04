package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionEngine;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Rawen.persistence.SharedDataManager;
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
 * Utilise SharedDataManager pour partager les données.
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
    private SharedDataManager sharedData;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            FirewallConfig config = new FirewallConfig();
            decisionEngine = new DecisionEngine(config);
            sharedData = SharedDataManager.getInstance();
            
            setupButtons();
            saveToBlockchainBtn.setDisable(true);
            
            System.out.println("✓ FinalDecisionController initialisé");
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Erreur: " + e.getMessage());
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
            showWarning("Pas de données", "Veuillez compléter l'analyse d'abord");
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
            showError("Erreur de décision", "Impossible de prendre la décision: " + e.getMessage());
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
     * Sauvegarde dans la blockchain ET les statistiques.
     */
    private void saveToBlockchain() {
        if (decision == null) {
            showWarning("Pas de décision", "Veuillez compléter l'analyse d'abord");
            return;
        }

        try {
            // Ajouter à la blockchain ET aux statistiques via SharedDataManager
            sharedData.addDecision(decision);
            
            // Afficher le résumé
            sharedData.printSummary();

            showSuccess("Sauvegarde réussie", 
                "✓ Décision sauvegardée dans la blockchain!\n" +
                "✓ Statistiques mises à jour!\n\n" +
                "Action: " + decision.getAction() + "\n" +
                "Score: " + decision.getTotalScore() + "/10");

            // Retourner au menu après un court délai
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            App.loadMainMenu();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (Exception e) {
            showError("Erreur de sauvegarde", "Impossible de sauvegarder: " + e.getMessage());
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
                    showError("Erreur de navigation", "Impossible de revenir en arrière");
                }
            });
        }

        if (saveToBlockchainBtn != null) {
            saveToBlockchainBtn.setOnAction(event -> saveToBlockchain());
        }
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

    public DecisionResult getDecision() {
        return decision;
    }
}