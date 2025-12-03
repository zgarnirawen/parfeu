package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Rawen.analyzer.DetectionSignal;
import com.mycompany.parfeu.Model.Rawen.analyzer.PacketAnalyzer;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionEngine;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la génération et l'analyse de paquets.
 */
public class PaquetgeneratorController implements Initializable {

    @FXML
    private Button ps; // Paquet Sain
    
    @FXML
    private Button pm; // Paquet Malicieux
    
    @FXML
    private Button firstAnalyzeButton;
    
    @FXML
    private Button backBtn;
    
    @FXML
    private TextArea packetDisplayArea;
    
    @FXML
    private TextArea firstAnalyzeResultArea;

    private Packet currentPacket;
    private FirewallConfig config;
    private PacketAnalyzer analyzer;
    private DecisionEngine decisionEngine;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialiser les composants
            config = new FirewallConfig();
            analyzer = new PacketAnalyzer(
                config.getMinPacketSize(),
                config.getMaxPacketSize(),
                config.getSuspiciousWords()
            );
            decisionEngine = new DecisionEngine(config);
            
            packetSelector.loadPacketFiles();
            
            setupButtonActions();
            
            System.out.println("✓ PaquetgeneratorController initialisé");
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible de charger les fichiers de paquets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupButtonActions() {
        // Bouton Back
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadMainMenu();
                } catch (IOException e) {
                    showError("Erreur de navigation", "Impossible de retourner au menu principal");
                    e.printStackTrace();
                }
            });
        }

        // Bouton Paquet Sain
        ps.setOnAction(event -> {
            try {
                currentPacket = packetSelector.selectRandomPacket(false);
                displayPacket(currentPacket, "🟢 PAQUET SAIN");
                firstAnalyzeResultArea.clear();
                firstAnalyzeResultArea.setPromptText("Cliquez sur 'Analyser le Paquet' pour voir les résultats.");
            } catch (Exception e) {
                showError("Erreur", "Impossible de générer un paquet sain: " + e.getMessage());
            }
        });

        // Bouton Paquet Malicieux
        pm.setOnAction(event -> {
            try {
                currentPacket = packetSelector.selectRandomPacket(true);
                displayPacket(currentPacket, "🔴 PAQUET MALICIEUX");
                firstAnalyzeResultArea.clear();
                firstAnalyzeResultArea.setPromptText("Cliquez sur 'Analyser le Paquet' pour voir les résultats.");
            } catch (Exception e) {
                showError("Erreur", "Impossible de générer un paquet malicieux: " + e.getMessage());
            }
        });

        // Bouton Première Analyse
        firstAnalyzeButton.setOnAction(event -> {
            if (currentPacket == null) {
                showWarning("Aucun paquet", "Veuillez d'abord générer un paquet en cliquant sur 'Paquet Sain' ou 'Paquet Malicieux'!");
                return;
            }
            
            try {
                performFirstAnalysis();
            } catch (Exception e) {
                showError("Erreur d'analyse", "Impossible d'analyser le paquet: " + e.getMessage());
            }
        });
    }

    /**
     * Affiche les informations du paquet.
     */
    private void displayPacket(Packet packet, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════════════╗\n");
        sb.append("║              ").append(type).append("              ║\n");
        sb.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        sb.append("🌐 INFORMATIONS RÉSEAU\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("  Source      : ").append(packet.getSrcIP()).append(":").append(packet.getSrcPort()).append("\n");
        sb.append("  Destination : ").append(packet.getDestIP()).append(":").append(packet.getDestPort()).append("\n");
        sb.append("  Protocole   : ").append(packet.getProtocol()).append("\n");
        sb.append("  Taille      : ").append(packet.getSize()).append(" bytes\n");
        sb.append("  Timestamp   : ").append(packet.getTimestamp()).append("\n\n");
        
        sb.append("📝 CONTENU (PAYLOAD)\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append(packet.getPayload()).append("\n\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        packetDisplayArea.setText(sb.toString());
    }

    /**
     * Effectue la première analyse (filtrage basique).
     */
    private void performFirstAnalysis() {
        // Analyse avec le moteur
        List<DetectionSignal> signals = analyzer.analyze(currentPacket);
        DecisionResult result = decisionEngine.decide(currentPacket, signals);
        
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════════════════════════════════╗\n");
        sb.append("║            🔍 RÉSULTAT DE L'ANALYSE                       ║\n");
        sb.append("╚═══════════════════════════════════════════════════════════╝\n\n");
        
        // Décision
        String actionSymbol = result.getAction().getSymbol();
        String actionColor = getActionEmoji(actionSymbol);
        sb.append("⚖️  DÉCISION : ").append(actionColor).append(" ").append(actionSymbol).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        
        // Score et niveau
        sb.append("📊 ÉVALUATION\n");
        sb.append("  Score de menace : ").append(result.getTotalScore()).append("/10\n");
        sb.append("  Niveau de risque: ").append(decisionEngine.evaluateRiskLevel(result.getTotalScore())).append("\n\n");
        
        // Raison
        sb.append("📋 RAISON\n");
        sb.append("  ").append(result.getReason()).append("\n\n");
        
        // Signaux détectés
        if (!signals.isEmpty()) {
            sb.append("⚠️  SIGNAUX DÉTECTÉS (").append(signals.size()).append(")\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (int i = 0; i < signals.size(); i++) {
                DetectionSignal signal = signals.get(i);
                sb.append("  ").append(i + 1).append(". ")
                  .append(signal.getDescription())
                  .append("\n     └─ Score: ").append(signal.getScore())
                  .append(" | Niveau: ").append(signal.getThreatLevel())
                  .append("\n\n");
            }
        } else {
            sb.append("✅ AUCUN SIGNAL DE MENACE\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("  Le paquet semble légitime et ne présente aucune\n");
            sb.append("  caractéristique suspecte.\n\n");
        }
        
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        firstAnalyzeResultArea.setText(sb.toString());
    }

    /**
     * Retourne l'emoji correspondant à l'action.
     */
    private String getActionEmoji(String action) {
        return switch (action) {
            case "[BLOCK]" -> "🚫";
            case "[ALERT]" -> "⚠️";
            case "[LOG]" -> "📝";
            case "[OK]" -> "✅";
            default -> "❓";
        };
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
}