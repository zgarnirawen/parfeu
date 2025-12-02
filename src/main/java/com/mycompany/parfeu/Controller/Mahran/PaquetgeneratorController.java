package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.generator.PacketSelector;
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
    private TextArea packetDisplayArea;
    
    @FXML
    private TextArea firstAnalyzeResultArea;

    private PacketSelector packetSelector;
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
            
            packetSelector = new PacketSelector();
            packetSelector.loadPacketFiles();
            
            setupButtonActions();
            
            System.out.println("✓ PaquetgeneratorController initialisé");
        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible de charger les fichiers de paquets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupButtonActions() {
        // Bouton Paquet Sain
        ps.setOnAction(event -> {
            try {
                currentPacket = packetSelector.selectRandomPacket(false);
                displayPacket(currentPacket, "🟢 PAQUET SAIN");
                firstAnalyzeResultArea.clear();
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
            } catch (Exception e) {
                showError("Erreur", "Impossible de générer un paquet malicieux: " + e.getMessage());
            }
        });

        // Bouton Première Analyse
        firstAnalyzeButton.setOnAction(event -> {
            if (currentPacket == null) {
                showWarning("Aucun paquet", "Veuillez d'abord générer un paquet!");
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
        sb.append("═══════════════════════════════════════\n");
        sb.append("         ").append(type).append("\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append("📍 Source      : ").append(packet.getSrcIP()).append(":").append(packet.getSrcPort()).append("\n");
        sb.append("📍 Destination : ").append(packet.getDestIP()).append(":").append(packet.getDestPort()).append("\n");
        sb.append("🔌 Protocole   : ").append(packet.getProtocol()).append("\n");
        sb.append("📦 Taille      : ").append(packet.getSize()).append(" bytes\n");
        sb.append("🕒 Timestamp   : ").append(packet.getTimestamp()).append("\n\n");
        sb.append("📝 Payload:\n");
        sb.append(packet.getPayload()).append("\n");
        
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
        sb.append("═══════════════════════════════════════\n");
        sb.append("      RÉSULTAT DE L'ANALYSE\n");
        sb.append("═══════════════════════════════════════\n\n");
        
        sb.append("⚖️  Action     : ").append(result.getAction().getSymbol()).append("\n");
        sb.append("📊 Score       : ").append(result.getTotalScore()).append("/10\n");
        sb.append("🎯 Niveau      : ").append(decisionEngine.evaluateRiskLevel(result.getTotalScore())).append("\n\n");
        
        sb.append("📋 Raison:\n");
        sb.append("   ").append(result.getReason()).append("\n\n");
        
        if (!signals.isEmpty()) {
            sb.append("⚠️  Signaux détectés (").append(signals.size()).append("):\n");
            for (int i = 0; i < signals.size(); i++) {
                sb.append("   ").append(i + 1).append(". ")
                  .append(signals.get(i).getDescription())
                  .append(" [Score: ").append(signals.get(i).getScore()).append("]\n");
            }
        } else {
            sb.append("✅ Aucun signal de menace détecté\n");
        }
        
        firstAnalyzeResultArea.setText(sb.toString());
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