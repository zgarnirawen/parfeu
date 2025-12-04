package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Rawen.blockchain.Block;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockChain;
import com.mycompany.parfeu.Model.Rawen.blockchain.BlockchainTableData;
import com.mycompany.parfeu.Model.Rawen.persistence.SharedDataManager;
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
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la vue blockchain avec TableView.
 * Utilise SharedDataManager pour les données partagées.
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
    private SharedDataManager sharedData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("\n🔄 ========== BLOCKCHAIN CONTROLLER INIT ==========");
        
        // Utiliser la blockchain partagée
        sharedData = SharedDataManager.getInstance();
        blockchain = sharedData.getBlockchain();
        tableData = FXCollections.observableArrayList();
        
        // Configurer la table
        setupTable();
        
        // Charger les données
        loadBlockchain();
        
        // Configurer les boutons
        setupButtons();
        
        System.out.println("✅ BlockchainController initialisé avec " + blockchain.getSize() + " blocs");
        System.out.println("================================================\n");
    }

    /**
     * Configure le TableView.
     */
    private void setupTable() {
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
                    showError("Erreur de navigation", "Impossible de retourner au menu");
                }
            });
        }
        
        if (refreshBtn != null) {
            refreshBtn.setOnAction(event -> {
                loadBlockchain();
                showInfo("Rafraîchi", "Blockchain rechargée avec succès!\n\nTotal blocs: " + blockchain.getSize());
            });
        }
        
        if (verifyBtn != null) {
            verifyBtn.setOnAction(event -> {
                boolean isValid = blockchain.isChainValid();
                if (isValid) {
                    showInfo("✓ Blockchain Valide", 
                        "L'intégrité de la blockchain a été vérifiée!\n\n" +
                        "✓ Tous les blocs sont valides\n" +
                        "✓ Tous les hashes sont corrects\n" +
                        "✓ La chaîne est immuable\n\n" +
                        "Total blocs: " + blockchain.getSize());
                } else {
                    showError("✗ Blockchain Invalide", 
                        "⚠️ ATTENTION: La blockchain a été compromise!\n\n" +
                        "Un ou plusieurs blocs ont été modifiés.");
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
        fileChooser.setTitle("Exporter la Blockchain");
        fileChooser.setInitialFileName("blockchain_export_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
        
        File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Header
                writer.write(Block.getCSVHeader() + "\n");
                
                // Data
                for (Block block : blockchain.getChain()) {
                    writer.write(block.toCSV() + "\n");
                }
                
                showInfo("Export Réussi", 
                    "Blockchain exportée avec succès!\n\n" +
                    "Fichier: " + file.getName() + "\n" +
                    "Emplacement: " + file.getParent() + "\n" +
                    "Blocs exportés: " + blockchain.getSize());
                
            } catch (IOException e) {
                showError("Erreur d'Export", "Impossible d'exporter: " + e.getMessage());
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