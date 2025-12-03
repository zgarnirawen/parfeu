package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.generator.PaquetMalicieux;
import com.mycompany.parfeu.Model.Mahran.generator.PaquetSimple;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockChain;
import com.mycompany.parfeu.Model.Rawen.decision.Actions;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class BlockchainController implements Initializable {

    @FXML private VBox blocksContainer;
    @FXML private HBox inforow;
    @FXML private Button backBtn;
    @FXML private Button refreshBtn;
    @FXML private Button verifyBtn;

    private BlockChain blockchain;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("\n🔄 ========== INITIALISATION BLOCKCHAIN ==========");
        
        blockchain = new BlockChain();
        addTestData();
        
        System.out.println("📊 Composants FXML:");
        System.out.println("  - blocksContainer: " + (blocksContainer != null ? "✓" : "✗ NULL"));
        System.out.println("  - inforow: " + (inforow != null ? "✓" : "✗ NULL"));
        System.out.println("  - backBtn: " + (backBtn != null ? "✓" : "✗ NULL"));
        System.out.println("  - refreshBtn: " + (refreshBtn != null ? "✓" : "✗ NULL"));
        System.out.println("  - verifyBtn: " + (verifyBtn != null ? "✓" : "✗ NULL"));
        
        setupButtons();
        loadBlockchain();
        
        System.out.println("✅ BlockchainController initialisé avec " + blockchain.getSize() + " blocs");
        System.out.println("================================================\n");
    }

    private void addTestData() {
        System.out.println("📦 Ajout de données de test...");
        
        List<DecisionResult> block1Decisions = new ArrayList<>();
        
        Packet p1 = new PaquetSimple("192.168.1.10", "203.0.113.10", 54321, 80, "HTTP", "GET /index.html HTTP/1.1");
        block1Decisions.add(new DecisionResult(p1, new ArrayList<>(), 0, Actions.ACCEPT, "Trafic normal"));
        
        Packet p2 = new PaquetSimple("192.168.1.20", "203.0.113.20", 54322, 443, "HTTPS", "POST /api/data");
        block1Decisions.add(new DecisionResult(p2, new ArrayList<>(), 3, Actions.ALERT, "Contenu suspect"));
        
        Packet p3 = new PaquetMalicieux("192.168.1.100", "203.0.113.30", 55000, 3306, "TCP", 
                                        "SELECT * FROM users WHERE id=1 OR 1=1", "SQL_INJECTION");
        block1Decisions.add(new DecisionResult(p3, new ArrayList<>(), 8, Actions.DROP, "SQL Injection détectée"));
        
        blockchain.addBlock(block1Decisions);
        
        List<DecisionResult> block2Decisions = new ArrayList<>();
        Packet p4 = new PaquetSimple("10.0.0.5", "203.0.113.40", 54350, 22, "SSH", "SSH-2.0-OpenSSH_8.2");
        block2Decisions.add(new DecisionResult(p4, new ArrayList<>(), 1, Actions.LOG, "Connexion SSH surveillée"));
        
        blockchain.addBlock(block2Decisions);
        
        System.out.println("  ✓ Bloc 1: " + block1Decisions.size() + " décisions");
        System.out.println("  ✓ Bloc 2: " + block2Decisions.size() + " décision");
    }

    private void loadBlockchain() {
        System.out.println("\n📊 Chargement de la blockchain...");
        
        if (blocksContainer == null) {
            System.err.println("❌ ERREUR: blocksContainer est NULL!");
            return;
        }
        
        blocksContainer.getChildren().clear();
        updateInfoRow();
        
        List<Block> chain = blockchain.getChain();
        System.out.println("  📦 Nombre de blocs à charger: " + chain.size());
        
        for (int i = 0; i < chain.size(); i++) {
            Block block = chain.get(i);
            System.out.println("\n  🔷 Chargement Bloc #" + block.index());
            
            try {
                VBox blockCard = createBlockCard(block);
                if (blockCard != null) {
                    blocksContainer.getChildren().add(blockCard);
                    System.out.println("    ✅ Carte ajoutée à l'interface");
                } else {
                    System.err.println("    ❌ blockCard est NULL");
                }
            } catch (Exception e) {
                System.err.println("    ❌ ERREUR: " + e.getMessage());
                e.printStackTrace();
                VBox errorCard = createErrorCard(block, e);
                blocksContainer.getChildren().add(errorCard);
            }
        }
        
        System.out.println("\n✅ Interface mise à jour: " + blocksContainer.getChildren().size() + " cartes affichées");
    }

    private VBox createBlockCard(Block block) throws IOException {
        System.out.println("    🎨 Création carte pour bloc #" + block.index());
        
        try {
            String fxmlPath = "/com/mycompany/parfeu/Views/Rawen/block_card.fxml";
            System.out.println("       📄 Chargement FXML: " + fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            VBox blockCard = loader.load();
            
            System.out.println("       ✓ FXML chargé");
            
            Block_cardController controller = loader.getController();
            if (controller != null) {
                System.out.println("       ✓ Contrôleur obtenu");
                controller.setBlockData(block);
                System.out.println("       ✓ Données définies");
            } else {
                System.err.println("       ⚠️ Contrôleur est NULL");
            }
            
            return blockCard;
            
        } catch (IOException e) {
            System.err.println("       ❌ IOException: " + e.getMessage());
            throw e;
        }
    }

    private VBox createErrorCard(Block block, Exception e) {
        VBox errorCard = new VBox(10);
        errorCard.setStyle(
            "-fx-background-color: #ffebee; " +
            "-fx-border-color: #f44336; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 20;"
        );
        
        Label errorTitle = new Label("❌ Erreur - Bloc #" + block.index());
        errorTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #c62828;");
        
        Label errorMsg = new Label("Impossible de charger ce bloc: " + e.getMessage());
        errorMsg.setStyle("-fx-text-fill: #d32f2f;");
        
        Label blockInfo = new Label("Hash: " + truncateHash(block.hash()));
        blockInfo.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");
        
        errorCard.getChildren().addAll(errorTitle, errorMsg, blockInfo);
        return errorCard;
    }

    private void updateInfoRow() {
        if (inforow == null || inforow.getChildren().size() < 3) {
            System.err.println("⚠️ inforow non initialisé correctement");
            return;
        }
        
        try {
            Label blocksLabel = (Label) inforow.getChildren().get(0);
            blocksLabel.setText("Blocks: " + blockchain.getSize());
            
            Label statusLabel = (Label) inforow.getChildren().get(1);
            boolean isValid = blockchain.isChainValid();
            statusLabel.setText("Status: " + (isValid ? "✓ Valid" : "✗ Invalid"));
            statusLabel.setStyle(isValid ? 
                "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 15;" : 
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 15;");
            
            Label lastBlockLabel = (Label) inforow.getChildren().get(2);
            Block lastBlock = blockchain.getLastBlock();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date(lastBlock.timestamp()));
            lastBlockLabel.setText("Last Block: " + timestamp);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur updateInfoRow: " + e.getMessage());
        }
    }

    private void setupButtons() {
        System.out.println("🔧 Configuration des boutons...");
        
        if (backBtn != null) {
            System.out.println("  ✓ Configuration bouton Back");
            backBtn.setOnAction(event -> {
                System.out.println("🔙 Clic sur Back");
                try {
                    App.loadMainMenu();
                } catch (IOException e) {
                    System.err.println("❌ Erreur navigation: " + e.getMessage());
                    showError("Erreur", "Impossible de retourner au menu principal");
                }
            });
        } else {
            System.err.println("  ⚠️ backBtn est NULL");
        }
        
        if (refreshBtn != null) {
            System.out.println("  ✓ Configuration bouton Refresh");
            refreshBtn.setOnAction(event -> {
                System.out.println("🔄 Clic sur Refresh");
                loadBlockchain();
                showInfo("✓ Rafraîchi", "La blockchain a été rechargée avec succès");
            });
        } else {
            System.err.println("  ⚠️ refreshBtn est NULL");
        }
        
        if (verifyBtn != null) {
            System.out.println("  ✓ Configuration bouton Verify");
            verifyBtn.setOnAction(event -> {
                System.out.println("✔️ Clic sur Verify");
                boolean isValid = blockchain.isChainValid();
                if (isValid) {
                    showInfo("✓ Blockchain Valide", 
                        "L'intégrité de la blockchain a été vérifiée avec succès.\n\n" +
                        "✓ Tous les blocs sont valides\n" +
                        "✓ Les hash sont corrects\n" +
                        "✓ La chaîne est intègre\n\n" +
                        "Nombre total de blocs: " + blockchain.getSize());
                } else {
                    showError("✗ Blockchain Invalide", 
                        "⚠️ ATTENTION: La blockchain a été compromise!\n\n" +
                        "Un ou plusieurs blocs ont été modifiés.");
                }
                updateInfoRow();
            });
        } else {
            System.err.println("  ⚠️ verifyBtn est NULL");
        }
        
        System.out.println("✅ Configuration des boutons terminée");
    }

    public void addDecisionToBlockchain(DecisionResult decision) {
        blockchain.addDecision(decision);
        loadBlockchain();
    }

    private String truncateHash(String hash) {
        if (hash == null || hash.length() <= 16) return hash;
        return hash.substring(0, 8) + "..." + hash.substring(hash.length() - 8);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public BlockChain getBlockchain() {
        return blockchain;
    }
}