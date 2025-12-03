package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Mahran.generator.Packet;
import com.mycompany.parfeu.Model.Mahran.generator.PaquetMalicieux;
import com.mycompany.parfeu.Model.Mahran.generator.PaquetSimple;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockChain;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockchainTableData;
import com.mycompany.parfeu.Model.Rawen.decision.Actions;
import com.mycompany.parfeu.Model.Rawen.decision.DecisionResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la vue blockchain avec TableView.
 */
public class BlockchainController implements Initializable {

    @FXML private Label totalBlocksLabel;
    @FXML private Label chainStatusLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private TableView<BlockchainTableData> blockchainTable;
    @FXML private TableColumn<BlockchainTableData, Integer> colBlockIndex;
    @FXML private TableColumn<BlockchainTableData, String> colTimestamp;
    @FXML private TableColumn<BlockchainTableData, String> colSrcIP;
    @FXML private TableColumn<BlockchainTableData, String> colDestIP;
    @FXML private TableColumn<BlockchainTableData, String> colProtocol;
    @FXML private TableColumn<BlockchainTableData, Integer> colDecisions;
    @FXML private TableColumn<BlockchainTableData, String> colHash;
    @FXML private Button backBtn;
    @FXML private Button refreshBtn;
    @FXML private Button verifyBtn;
    @FXML private Button exportBtn;

    private BlockChain blockchain;
    private ObservableList<BlockchainTableData> tableData;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("\n🔄 ========== BLOCKCHAIN CONTROLLER INIT ==========");
        
        blockchain = new BlockChain();
        tableData = FXCollections.observableArrayList();
        
        // Ajouter des données de test
        addTestData();
        
        // Configurer la table
        setupTable();
        
        // Charger les données
        loadBlockchain();
        
        // Configurer les boutons
        setupButtons();
        
        System.out.println("✅ BlockchainController initialized with " + blockchain.getSize() + " blocks");
        System.out.println("================================================\n");
    }

    /**
     * Configure le TableView.
     */
    private void setupTable() {
        // Le cellValueFactory est déjà défini dans le FXML
        // Mais on peut le redéfinir ici si nécessaire
        
        // Style pour les lignes selon le type de bloc
        blockchainTable.setRowFactory(tv -> new TableRow<BlockchainTableData>() {
            @Override
            protected void updateItem(BlockchainTableData item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setStyle("");
                } else {
                    if (item.getIndex() == 0) {
                        // Genesis block - vert
                        setStyle("-fx-background-color: #d4edda;");
                    } else if (item.getDecisionsCount() == 0) {
                        // Bloc vide - gris
                        setStyle("-fx-background-color: #f8f9fa;");
                    } else {
                        // Bloc normal - blanc
                        setStyle("-fx-background-color: white;");
                    }
                }
            }
        });
        
        blockchainTable.setItems(tableData);
    }

    /**
     * Ajoute des données de test.
     */
    private void addTestData() {
        System.out.println("📦 Adding test data...");
        
        // Bloc 1 - Paquets normaux et suspects
        List<DecisionResult> block1Decisions = new ArrayList<>();
        
        Packet p1 = new PaquetSimple("192.168.1.10", "203.0.113.10", 54321, 80, "HTTP", "GET /index.html HTTP/1.1");
        block1Decisions.add(new DecisionResult(p1, new ArrayList<>(), 0, Actions.ACCEPT, "Normal traffic"));
        
        Packet p2 = new PaquetSimple("192.168.1.20", "203.0.113.20", 54322, 443, "HTTPS", "POST /api/data");
        block1Decisions.add(new DecisionResult(p2, new ArrayList<>(), 3, Actions.ALERT, "Suspicious content"));
        
        Packet p3 = new PaquetMalicieux("192.168.1.100", "203.0.113.30", 55000, 3306, "TCP", 
                                        "SELECT * FROM users WHERE id=1 OR 1=1", "SQL_INJECTION");
        block1Decisions.add(new DecisionResult(p3, new ArrayList<>(), 8, Actions.DROP, "SQL Injection detected"));
        
        blockchain.addBlock(block1Decisions);
        
        // Bloc 2 - Connexions SSH
        List<DecisionResult> block2Decisions = new ArrayList<>();
        Packet p4 = new PaquetSimple("10.0.0.5", "203.0.113.40", 54350, 22, "SSH", "SSH-2.0-OpenSSH_8.2");
        block2Decisions.add(new DecisionResult(p4, new ArrayList<>(), 1, Actions.LOG, "SSH connection monitored"));
        
        blockchain.addBlock(block2Decisions);
        
        // Bloc 3 - Trafic FTP
        List<DecisionResult> block3Decisions = new ArrayList<>();
        Packet p5 = new PaquetSimple("172.16.0.10", "203.0.113.50", 54400, 21, "FTP", "USER anonymous");
        block3Decisions.add(new DecisionResult(p5, new ArrayList<>(), 2, Actions.LOG, "FTP access logged"));
        
        blockchain.addBlock(block3Decisions);
        
        System.out.println("  ✓ Added 3 test blocks");
    }

    /**
     * Charge la blockchain dans le TableView.
     */
    private void loadBlockchain() {
        System.out.println("\n📊 Loading blockchain into TableView...");
        
        tableData.clear();
        List<Block> chain = blockchain.getChain();
        
        System.out.println("  📦 Total blocks: " + chain.size());
        
        for (Block block : chain) {
            BlockchainTableData data = new BlockchainTableData(block);
            tableData.add(data);
            System.out.println("    ✓ Loaded Block #" + block.index());
        }
        
        updateInfoPanel();
        
        System.out.println("✅ TableView updated with " + tableData.size() + " entries\n");
    }

    /**
     * Met à jour le panneau d'information.
     */
    private void updateInfoPanel() {
        totalBlocksLabel.setText(String.valueOf(blockchain.getSize()));
        
        boolean isValid = blockchain.isChainValid();
        chainStatusLabel.setText(isValid ? "✓ VALID" : "✗ INVALID");
        chainStatusLabel.setStyle(isValid ? 
            "-fx-text-fill: #27ae60; -fx-font-weight: bold;" : 
            "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        Block lastBlock = blockchain.getLastBlock();
        String lastUpdate = dateFormat.format(new Date(lastBlock.timestamp()));
        lastUpdateLabel.setText(lastUpdate);
    }

    /**
     * Configure les boutons.
     */
    private void setupButtons() {
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadMainMenu();
                } catch (IOException e) {
                    showError("Navigation Error", "Cannot return to main menu");
                }
            });
        }
        
        if (refreshBtn != null) {
            refreshBtn.setOnAction(event -> {
                loadBlockchain();
                showInfo("Refreshed", "Blockchain reloaded successfully");
            });
        }
        
        if (verifyBtn != null) {
            verifyBtn.setOnAction(event -> {
                boolean isValid = blockchain.isChainValid();
                if (isValid) {
                    showInfo("✓ Chain Valid", 
                        "Blockchain integrity verified successfully!\n\n" +
                        "✓ All blocks are valid\n" +
                        "✓ All hashes are correct\n" +
                        "✓ Chain is immutable\n\n" +
                        "Total blocks: " + blockchain.getSize());
                } else {
                    showError("✗ Chain Invalid", 
                        "⚠️ WARNING: Blockchain has been compromised!\n\n" +
                        "One or more blocks have been modified.");
                }
                updateInfoPanel();
            });
        }
        
        if (exportBtn != null) {
            exportBtn.setOnAction(event -> exportToCSV());
        }
    }

    /**
     * Exporte la blockchain vers un fichier CSV.
     */
    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Blockchain");
        fileChooser.setInitialFileName("blockchain_export_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Header
                writer.write(Block.getCSVHeader() + "\n");
                
                // Data
                for (Block block : blockchain.getChain()) {
                    writer.write(block.toCSV() + "\n");
                }
                
                showInfo("Export Successful", 
                    "Blockchain exported to:\n" + file.getAbsolutePath());
                
            } catch (IOException e) {
                showError("Export Error", "Failed to export blockchain: " + e.getMessage());
            }
        }
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