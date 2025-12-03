package com.mycompany.parfeu.Controller.Mahran;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.config.FirewallConfig;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.initialPacketFiltering.IPFilter;
import com.mycompany.parfeu.Model.Mahran.initialPacketFiltering.PortFilter;
import com.mycompany.parfeu.Model.Mahran.initialPacketFiltering.ProtocolFilter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le filtrage initial des paquets.
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        config = new FirewallConfig();
        passedFiltering = false;
        setupButtons();
        System.out.println("✓ InitialFilteringController initialized");
    }

    /**
     * Définit le paquet à filtrer.
     */
    public void setPacket(Packet packet) {
        this.currentPacket = packet;
        displayPacketInfo();
        performFiltering();
    }

    /**
     * Affiche les informations du paquet.
     */
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
        
        sb.append("═══════════════════════════════════════════════════════════\n");

        packetInfoArea.setText(sb.toString());
    }

    /**
     * Effectue le filtrage initial.
     */
    private void performFiltering() {
        if (currentPacket == null) return;

        StringBuilder details = new StringBuilder();
        details.append("═══════════════════════════════════════════════════════════\n");
        details.append("              INITIAL FILTERING PROCESS\n");
        details.append("═══════════════════════════════════════════════════════════\n\n");

        boolean allPassed = true;

        // 1. IP Validation
        details.append("1️⃣ IP ADDRESS VALIDATION\n");
        details.append("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        boolean ipBlocked = config.getBlacklistedIPs().contains(currentPacket.getSrcIP()) ||
                           config.getBlacklistedIPs().contains(currentPacket.getDestIP());
        
        if (ipBlocked) {
            ipValidationLabel.setText("❌ BLOCKED");
            ipValidationLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            details.append("   ⚠️ IP address is BLACKLISTED\n");
            details.append("   Source: ").append(currentPacket.getSrcIP()).append("\n");
            allPassed = false;
        } else {
            ipValidationLabel.setText("✅ VALID");
            ipValidationLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            details.append("   ✅ IP addresses are valid\n");
            details.append("   Source: ").append(currentPacket.getSrcIP()).append(" - OK\n");
            details.append("   Destination: ").append(currentPacket.getDestIP()).append(" - OK\n");
        }

        // 2. Port Validation
        details.append("\n2️⃣ PORT VALIDATION\n");
        details.append("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        boolean validPorts = currentPacket.getSrcPort() >= 0 && currentPacket.getSrcPort() <= 65535 &&
                            currentPacket.getDestPort() >= 0 && currentPacket.getDestPort() <= 65535;
        
        if (!validPorts) {
            portValidationLabel.setText("❌ INVALID");
            portValidationLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            details.append("   ⚠️ Invalid port number detected\n");
            allPassed = false;
        } else {
            portValidationLabel.setText("✅ VALID");
            portValidationLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            details.append("   ✅ Port numbers are valid\n");
            details.append("   Source Port: ").append(currentPacket.getSrcPort()).append(" - OK\n");
            details.append("   Dest Port: ").append(currentPacket.getDestPort()).append(" - OK\n");
        }

        // 3. Protocol Check
        details.append("\n3️⃣ PROTOCOL CHECK\n");
        details.append("   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        String protocol = currentPacket.getProtocol();
        if (protocol != null && !protocol.isEmpty()) {
            protocolCheckLabel.setText("✅ VALID");
            protocolCheckLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            details.append("   ✅ Protocol is recognized\n");
            details.append("   Protocol: ").append(protocol).append(" - OK\n");
        } else {
            protocolCheckLabel.setText("⚠️ UNKNOWN");
            protocolCheckLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
            details.append("   ⚠️ Protocol is unknown or unspecified\n");
        }

        // Overall Status
        details.append("\n═══════════════════════════════════════════════════════════\n");
        details.append("                   FINAL RESULT\n");
        details.append("═══════════════════════════════════════════════════════════\n");
        
        passedFiltering = allPassed;
        
        if (allPassed) {
            overallStatusLabel.setText("✅ PASSED");
            overallStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 18;");
            details.append("\n✅ Packet PASSED initial filtering\n");
            details.append("   → Ready for deep analysis\n");
            analyzeBtn.setDisable(false);
        } else {
            overallStatusLabel.setText("❌ BLOCKED");
            overallStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 18;");
            details.append("\n❌ Packet BLOCKED by initial filtering\n");
            details.append("   → Analysis not required\n");
            analyzeBtn.setDisable(true);
        }

        filterDetailsArea.setText(details.toString());
    }

    /**
     * Configure les boutons.
     */
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
            analyzeBtn.setOnAction(event -> {
                if (passedFiltering && currentPacket != null) {
                    try {
                        App.loadScene("/com/mycompany/parfeu/Views/Rawen/deep_analysis.fxml", 900, 800);
                        // Pass packet to next controller (implement via App class or shared state)
                    } catch (IOException e) {
                        showError("Navigation Error", "Cannot proceed to analysis");
                    }
                }
            });
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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