package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Rawen.analyzer.PacketAnalyzer;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionEngine;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'analyse approfondie des paquets.
 */
public class DeepAnalysisController implements Initializable {

    @FXML private Label threatScoreLabel;
    @FXML private Label riskLevelLabel;
    @FXML private ProgressBar threatProgressBar;
    @FXML private ListView<String> signalsListView;
    @FXML private TextArea analysisDetailsArea;
    @FXML private Button backBtn;
    @FXML private Button decisionBtn;

    private Packet currentPacket;
    private List<DetectionSignal> detectedSignals;
    private int totalScore;
    private FirewallConfig config;
    private PacketAnalyzer analyzer;
    private DecisionEngine decisionEngine;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        config = new FirewallConfig();
        analyzer = new PacketAnalyzer(
            config.getMinPacketSize(),
            config.getMaxPacketSize(),
            config.getSuspiciousWords()
        );
        decisionEngine = new DecisionEngine(config);
        
        setupButtons();
        decisionBtn.setDisable(true);
        
        System.out.println("✓ DeepAnalysisController initialized");
    }

    /**
     * Définit le paquet à analyser et lance l'analyse automatiquement.
     */
    public void setPacket(Packet packet) {
        this.currentPacket = packet;
        performAnalysis();
    }

    /**
     * Effectue l'analyse approfondie.
     */
    private void performAnalysis() {
        if (currentPacket == null) {
            showWarning("No Packet", "Please generate a packet first");
            return;
        }

        try {
            // Analyse du paquet
            detectedSignals = analyzer.analyze(currentPacket);
            totalScore = analyzer.calculateTotalScore(detectedSignals);

            // Mise à jour de l'interface
            updateThreatScore();
            displaySignals();
            displayAnalysisDetails();
            
            // Activer le bouton de décision
            decisionBtn.setDisable(false);

        } catch (Exception e) {
            showError("Analysis Error", "Failed to analyze packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour le score de menace.
     */
    private void updateThreatScore() {
        threatScoreLabel.setText(String.valueOf(totalScore));
        
        // Déterminer le niveau de risque
        String riskLevel = decisionEngine.evaluateRiskLevel(totalScore);
        riskLevelLabel.setText(riskLevel);
        
        // Couleur selon le niveau
        String color = switch (riskLevel) {
            case "SAFE" -> "#27ae60";
            case "LOW" -> "#3498db";
            case "MEDIUM" -> "#f39c12";
            case "HIGH" -> "#e67e22";
            case "CRITICAL" -> "#e74c3c";
            default -> "#95a5a6";
        };
        
        threatScoreLabel.setStyle("-fx-text-fill: " + color + ";");
        riskLevelLabel.setStyle("-fx-text-fill: " + color + ";");
        
        // Progress bar
        double progress = Math.min(totalScore / 10.0, 1.0);
        threatProgressBar.setProgress(progress);
        threatProgressBar.setStyle("-fx-accent: " + color + ";");
    }

    /**
     * Affiche les signaux détectés.
     */
    private void displaySignals() {
        signalsListView.getItems().clear();
        
        if (detectedSignals.isEmpty()) {
            signalsListView.getItems().add("✅ No threats detected - Packet appears legitimate");
        } else {
            for (int i = 0; i < detectedSignals.size(); i++) {
                DetectionSignal signal = detectedSignals.get(i);
                String emoji = signal.isCritical() ? "🔴" : "🟡";
                String item = String.format("%s Signal %d: %s (Score: %d, Level: %s)",
                    emoji, i + 1, signal.getDescription(), 
                    signal.getScore(), signal.getThreatLevel());
                signalsListView.getItems().add(item);
            }
        }
    }

    /**
     * Affiche les détails de l'analyse.
     */
    private void displayAnalysisDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("              DEEP ANALYSIS REPORT\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        
        sb.append("📦 PACKET SUMMARY\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  Source      : ").append(currentPacket.getSrcIP())
          .append(":").append(currentPacket.getSrcPort()).append("\n");
        sb.append("  Destination : ").append(currentPacket.getDestIP())
          .append(":").append(currentPacket.getDestPort()).append("\n");
        sb.append("  Protocol    : ").append(currentPacket.getProtocol()).append("\n");
        sb.append("  Size        : ").append(currentPacket.getSize()).append(" bytes\n\n");
        
        sb.append("🔍 DETECTION RESULTS\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  Total Score    : ").append(totalScore).append(" / 10\n");
        sb.append("  Risk Level     : ").append(decisionEngine.evaluateRiskLevel(totalScore)).append("\n");
        sb.append("  Signals Found  : ").append(detectedSignals.size()).append("\n\n");
        
        if (!detectedSignals.isEmpty()) {
            sb.append("⚠️ DETECTED THREATS\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (int i = 0; i < detectedSignals.size(); i++) {
                DetectionSignal signal = detectedSignals.get(i);
                sb.append(String.format("\n  Signal %d:\n", i + 1));
                sb.append("    Description : ").append(signal.getDescription()).append("\n");
                sb.append("    Score       : ").append(signal.getScore()).append(" / 10\n");
                sb.append("    Threat Level: ").append(signal.getThreatLevel()).append("\n");
                sb.append("    Critical    : ").append(signal.isCritical() ? "YES ⚠️" : "NO").append("\n");
            }
        } else {
            sb.append("✅ NO THREATS DETECTED\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("  The packet appears to be legitimate with no\n");
            sb.append("  suspicious characteristics detected.\n");
        }
        
        sb.append("\n═══════════════════════════════════════════════════════════\n");
        sb.append("  Analysis completed at: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("═══════════════════════════════════════════════════════════\n");
        
        analysisDetailsArea.setText(sb.toString());
    }

    /**
     * Configure les boutons.
     */
    private void setupButtons() {
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadScene("/com/mycompany/parfeu/Views/Mahran/initial_filtering.fxml", 900, 800);
                } catch (IOException e) {
                    showError("Navigation Error", "Cannot return to previous page");
                }
            });
        }

        if (decisionBtn != null) {
            decisionBtn.setOnAction(event -> navigateToFinalDecision());
        }
    }

    /**
     * Navigation vers Final Decision.
     */
    private void navigateToFinalDecision() {
        if (currentPacket == null || detectedSignals == null) {
            showWarning("Cannot Proceed", "Analysis not completed");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/mycompany/parfeu/Views/Rawen/final_decision.fxml")
            );
            Parent root = loader.load();

            // Passer les données au contrôleur de décision finale
            FinalDecisionController controller = loader.getController();
            controller.setDecisionData(currentPacket, detectedSignals);

            // Charger la nouvelle scène
            Scene scene = new Scene(root, 900, 800);
            Stage stage = (Stage) decisionBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            showError("Navigation Error", "Cannot proceed to Final Decision: " + e.getMessage());
            e.printStackTrace();
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

    // Getters
    public Packet getCurrentPacket() { return currentPacket; }
    public List<DetectionSignal> getDetectedSignals() { return detectedSignals; }
    public int getTotalScore() { return totalScore; }
}