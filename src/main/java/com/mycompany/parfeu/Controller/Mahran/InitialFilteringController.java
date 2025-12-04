package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Controller.Rawen.FinalDecisionController;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import com.mycompany.parfeu.Model.Rawen.decision.Actions;
import com.mycompany.parfeu.Model.Rawen.persistence.SharedDataManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le filtrage initial - VERSION STRICTE
 * 🔥 BLOQUE immédiatement si IP blacklistée, port surveillé ou payload suspect
 */
public class InitialFilteringController implements Initializable {

    @FXML private TextArea packetInfoArea;
    @FXML private Label ipValidationLabel;
    @FXML private Label portValidationLabel;
    @FXML private Label protocolCheckLabel;
    @FXML private Label overallStatusLabel;
    @FXML private TextArea filterDetailsArea;
    @FXML private Button backBtn;
    @FXML private Button analyzeBtn;

    private Packet currentPacket;
    private FirewallConfig config;
    private boolean passedFiltering;
    private SharedDataManager sharedData;
    private String blockReason = null; // Raison du blocage

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sharedData = SharedDataManager.getInstance();
        config = sharedData.getConfiguration();
        passedFiltering = false;
        setupButtons();
        System.out.println("✓ InitialFilteringController initialized");
    }

    public void setPacket(Packet packet) {
        this.currentPacket = packet;
        displayPacketInfo();
        performFiltering();
    }

    private void displayPacketInfo() {
        if (currentPacket == null) {
            packetInfoArea.setText("No packet loaded.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("                  PACKET INFORMATION\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        
        sb.append("📍 SOURCE      : ").append(currentPacket.getSrcIP())
          .append(":").append(currentPacket.getSrcPort()).append("\n");
        sb.append("📍 DESTINATION : ").append(currentPacket.getDestIP())
          .append(":").append(currentPacket.getDestPort()).append("\n");
        sb.append("📡 PROTOCOL    : ").append(currentPacket.getProtocol()).append("\n");
        sb.append("📦 SIZE        : ").append(currentPacket.getSize()).append(" bytes\n");
        sb.append("🕐 TIMESTAMP   : ").append(currentPacket.getTimestamp()).append("\n\n");
        sb.append("📝 PAYLOAD     : ").append(currentPacket.getPayload().substring(0, 
            Math.min(100, currentPacket.getPayload().length()))).append("...\n\n");
        
        sb.append("═══════════════════════════════════════════════════════════\n");

        packetInfoArea.setText(sb.toString());
    }

    /**
     * 🔥 FILTRAGE STRICT avec blocage immédiat
     */
    private void performFiltering() {
        if (currentPacket == null) return;

        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════════════════════════════════\n");
        details.append("              INITIAL FILTERING PROCESS\n");
        details.append("═══════════════════════════════════════════════════════════\n\n");

        boolean allPassed = true;
        blockReason = null;

        // 1. 🔥 IP BLACKLIST CHECK (CRITIQUE)
        details.append("1️⃣ IP BLACKLIST VERIFICATION\n");
        details.append("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        boolean srcBlacklisted = config.getBlacklistedIPs().contains(currentPacket.getSrcIP());
        boolean destBlacklisted = config.getBlacklistedIPs().contains(currentPacket.getDestIP());
        
        if (srcBlacklisted || destBlacklisted) {
            ipValidationLabel.setText("❌ BLACKLISTED");
            ipValidationLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            details.append("   🚨 CRITICAL: IP address is BLACKLISTED!\n");
            if (srcBlacklisted) {
                details.append("   Source IP: ").append(currentPacket.getSrcIP()).append(" ❌\n");
                blockReason = "Source IP is blacklisted: " + currentPacket.getSrcIP();
            }
            if (destBlacklisted) {
                details.append("   Destination IP: ").append(currentPacket.getDestIP()).append(" ❌\n");
                blockReason = "Destination IP is blacklisted: " + currentPacket.getDestIP();
            }
            details.append("   → Packet will be DROPPED immediately\n");
            allPassed = false;
        } else {
            ipValidationLabel.setText("✅ VALID");
            ipValidationLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            details.append("   ✅ IP addresses are valid\n");
            details.append("   Source: ").append(currentPacket.getSrcIP()).append(" ✓\n");
            details.append("   Destination: ").append(currentPacket.getDestIP()).append(" ✓\n");
        }

        // 2. 🔥 PORT MONITORING CHECK
        details.append("\n2️⃣ PORT MONITORING\n");
        details.append("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        boolean srcPortMonitored = config.getMonitoredPorts().contains(currentPacket.getSrcPort());
        boolean destPortMonitored = config.getMonitoredPorts().contains(currentPacket.getDestPort());
        
        // Ports < 1024 sont privilégiés
        boolean srcPortPrivileged = currentPacket.getSrcPort() < 1024;
        boolean destPortPrivileged = currentPacket.getDestPort() < 1024;
        
        if ((destPortMonitored || destPortPrivileged) && allPassed) {
            portValidationLabel.setText("⚠️ SUSPICIOUS");
            portValidationLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            details.append("   ⚠️ Suspicious port activity detected\n");
            if (destPortMonitored) {
                details.append("   Dest Port ").append(currentPacket.getDestPort())
                      .append(" is on MONITORED list\n");
            }
            if (destPortPrivileged) {
                details.append("   Dest Port ").append(currentPacket.getDestPort())
                      .append(" is PRIVILEGED (< 1024)\n");
            }
            details.append("   → Requires deep analysis\n");
            // On ne bloque pas encore, mais on note
        } else {
            portValidationLabel.setText("✅ VALID");
            portValidationLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            details.append("   ✅ Port numbers are valid\n");
            details.append("   Source Port: ").append(currentPacket.getSrcPort()).append(" ✓\n");
            details.append("   Dest Port: ").append(currentPacket.getDestPort()).append(" ✓\n");
        }

        // 3. 🔥 PAYLOAD QUICK SCAN
        details.append("\n3️⃣ PAYLOAD QUICK SCAN\n");
        details.append("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        String payload = currentPacket.getPayload().toLowerCase();
        boolean foundSuspiciousWord = false;
        
        for (String word : config.getSuspiciousWords()) {
            if (payload.contains(word.toLowerCase())) {
                foundSuspiciousWord = true;
                details.append("   🚨 FOUND: '").append(word).append("'\n");
            }
        }
        
        if (foundSuspiciousWord && allPassed) {
            protocolCheckLabel.setText("⚠️ SUSPICIOUS");
            protocolCheckLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            details.append("   ⚠️ Suspicious patterns detected in payload\n");
            details.append("   → Packet flagged for deep analysis\n");
        } else {
            protocolCheckLabel.setText("✅ CLEAN");
            protocolCheckLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            details.append("   ✅ No suspicious patterns found\n");
        }

        // 4. FINAL DECISION
        details.append("\n═══════════════════════════════════════════════════════════\n");
        details.append("                   FINAL RESULT\n");
        details.append("═══════════════════════════════════════════════════════════\n");
        
        passedFiltering = allPassed;
        
        if (!allPassed) {
            // 🔥 BLOCAGE IMMÉDIAT
            overallStatusLabel.setText("❌ BLOCKED");
            overallStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 18;");
            details.append("\n❌ Packet BLOCKED by initial filtering\n");
            details.append("   Reason: ").append(blockReason).append("\n");
            details.append("   → Deep analysis SKIPPED\n");
            details.append("   → Packet will be DROPPED immediately\n");
            analyzeBtn.setDisable(true);
            
            // 🔥 ENREGISTRER LA DÉCISION DE BLOCAGE IMMÉDIATEMENT
            blockPacketImmediately();
            
        } else {
            overallStatusLabel.setText("✅ PASSED");
            overallStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 18;");
            details.append("\n✅ Packet PASSED initial filtering\n");
            details.append("   → Ready for deep analysis\n");
            analyzeBtn.setDisable(false);
        }

        filterDetailsArea.setText(details.toString());
    }

    /**
     * 🔥 BLOCAGE IMMÉDIAT avec enregistrement dans la blockchain
     */
    private void blockPacketImmediately() {
        try {
            System.out.println("\n🚫 BLOCAGE IMMÉDIAT du paquet");
            System.out.println("   Raison: " + blockReason);
            
            // Créer une décision de blocage
            DecisionResult decision = new DecisionResult(
                currentPacket,
                new ArrayList<>(), // Pas de signaux (blocage au niveau filtrage)
                99, // Score max pour blocage immédiat
                Actions.DROP,
                blockReason
            );
            
            // Enregistrer dans la blockchain ET les statistiques
            sharedData.addDecision(decision);
            
            System.out.println("✓ Décision de blocage enregistrée dans la blockchain");
            
            // Afficher un message à l'utilisateur
            showBlockedAlert();
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'enregistrement du blocage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Alerte de blocage pour l'utilisateur
     */
    private void showBlockedAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🚫 Packet Blocked");
        alert.setHeaderText("Packet blocked by Initial Filtering");
        alert.setContentText(
            "The packet has been BLOCKED and will not proceed to deep analysis.\n\n" +
            "Reason: " + blockReason + "\n\n" +
            "This decision has been recorded in the blockchain.\n" +
            "You can view it in the History section."
        );
        alert.showAndWait();
    }

    private void setupButtons() {
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadScene("/com/mycompany/parfeu/Views/Mahran/guided_packet_input.fxml", 900, 800);
                } catch (IOException e) {
                    showError("Navigation Error", "Cannot return to previous page");
                }
            });
        }

        if (analyzeBtn != null) {
            analyzeBtn.setDisable(true);
            analyzeBtn.setOnAction(event -> navigateToDeepAnalysis());
        }
    }

    private void navigateToDeepAnalysis() {
        if (!passedFiltering || currentPacket == null) {
            showWarning("Cannot Proceed", "Packet did not pass initial filtering");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                App.class.getResource("/com/mycompany/parfeu/Views/Rawen/deep_analysis.fxml")
            );
            Parent root = loader.load();

            com.mycompany.parfeu.Controller.Rawen.DeepAnalysisController controller = loader.getController();
            controller.setPacket(currentPacket);

            Scene scene = new Scene(root, 900, 800);
            Stage stage = (Stage) analyzeBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            showError("Navigation Error", "Cannot proceed to Deep Analysis: " + e.getMessage());
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

    public Packet getCurrentPacket() {
        return currentPacket;
    }

    public boolean hasPassedFiltering() {
        return passedFiltering;
    }
}