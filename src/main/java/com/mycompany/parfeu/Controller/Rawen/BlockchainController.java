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
import javafx.scene.control.cell.PropertyValueFactory;
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
 * 🔥 VERSION FINALE - Chargement correct des données au démarrage
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
    @FXML private TableColumn<BlockchainTableData, String> colDecisions;
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
        System.out.println("\n🔗 ========== BLOCKCHAIN CONTROLLER INIT ==========");
        
        // 🔥 RÉCUPÉRER LA BLOCKCHAIN DÉJÀ CHARGÉE
        sharedData = SharedDataManager.getInstance();
        blockchain = sharedData.getBlockchain();
        tableData = FXCollections.observableArrayList();

        // Configuration des colonnes
        setupTableColumns();
        
        // 🔥 CHARGER LES DONNÉES
        loadBlockchain();
        
        // Configuration des boutons
        setupButtons();
        
        System.out.println("✅ BlockchainController initialisé");
        System.out.println("   📊 Blocs chargés: " + blockchain.getSize());
        System.out.println("================================================\n");
    }

    private void setupTableColumns() {
        System.out.println("🔧 Configuration des colonnes...");
        
        // Configuration des colonnes
        colBlockIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colSrcIP.setCellValueFactory(new PropertyValueFactory<>("srcIP"));
        colDestIP.setCellValueFactory(new PropertyValueFactory<>("destIP"));
        colProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        colDecisions.setCellValueFactory(new PropertyValueFactory<>("decisions"));
        colHash.setCellValueFactory(new PropertyValueFactory<>("hashShort"));

        // 🔥 STYLE DU TABLEAU - TEXTE NOIR VISIBLE
        if (blockchainTable != null) {
            blockchainTable.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: black !important;" +
                "-fx-font-size: 13px;"
            );
            
            blockchainTable.setItems(tableData);
        }

        // 🔥 COULEURS DES LIGNES avec texte noir
        blockchainTable.setRowFactory(tv -> new TableRow<BlockchainTableData>() {
            @Override
            protected void updateItem(BlockchainTableData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String baseStyle = "-fx-text-fill: #000000; -fx-font-weight: normal;";
                    
                    if (item.isGenesis()) {
                        setStyle(baseStyle + "-fx-background-color: #C8E6C9;");
                    } else if (item.getDecisions().contains("DROP")) {
                        setStyle(baseStyle + "-fx-background-color: #FFCDD2;");
                    } else if (item.getDecisions().contains("ALERT")) {
                        setStyle(baseStyle + "-fx-background-color: #FFE0B2;");
                    } else if (item.getDecisions().contains("ACCEPT")) {
                        setStyle(baseStyle + "-fx-background-color: #E8F5E9;");
                    } else {
                        setStyle(baseStyle + "-fx-background-color: #ffffff;");
                    }
                }
            }
        });
        
        // 🔥 STYLE DES COLONNES - Texte noir
        String columnStyle = "-fx-text-fill: black; -fx-alignment: CENTER;";
        colBlockIndex.setStyle(columnStyle);
        colTimestamp.setStyle(columnStyle);
        colSrcIP.setStyle(columnStyle);
        colDestIP.setStyle(columnStyle);
        colProtocol.setStyle(columnStyle);
        colDecisions.setStyle(columnStyle + "-fx-font-weight: bold;");
        colHash.setStyle(columnStyle);
        
        System.out.println("  ✓ Colonnes configurées avec texte noir visible");
    }

    /**
     * 🔥 CHARGEMENT DES DONNÉES depuis la blockchain reconstruite
     */
    private void loadBlockchain() {
        System.out.println("\n📦 Chargement de la blockchain...");
        
        tableData.clear();
        List<Block> chain = blockchain.getChain();
        
        System.out.println("   📊 Nombre de blocs dans la chaîne: " + chain.size());
        
        if (chain.isEmpty()) {
            System.out.println("   ⚠️ Blockchain vide!");
            showInfo("Blockchain vide", "La blockchain ne contient que le bloc Genesis.\n\nCommencez à analyser des paquets pour voir l'historique.");
            return;
        }
        
        // Ajouter chaque bloc au tableau
        int count = 0;
        for (Block block : chain) {
            BlockchainTableData data = new BlockchainTableData(block);
            tableData.add(data);
            count++;
            
            System.out.println("   ✓ Bloc #" + block.index() + 
                             " | " + data.getSrcIP() + 
                             " -> " + data.getDestIP() + 
                             " | " + data.getProtocol() +
                             " | Decisions: " + data.getDecisions());
        }
        
        System.out.println("   ✅ " + count + " blocs chargés dans le tableau");
        
        // Forcer le rafraîchissement
        blockchainTable.refresh();
        
        // Mettre à jour le panneau d'info
        updateInfoPanel();
        
        // 🔥 MESSAGE SI DONNÉES CHARGÉES
        if (count > 1) { // Plus que le Genesis
            showInfo("Données restaurées", 
                "✅ Blockchain restaurée avec succès!\n\n" +
                "Total de blocs: " + count + "\n" +
                "Historique chargé depuis la session précédente.");
        }
    }

    private void updateInfoPanel() {
        totalBlocksLabel.setText(String.valueOf(blockchain.getSize()));

        boolean isValid = blockchain.isChainValid();
        chainStatusLabel.setText(isValid ? "✓ VALID" : "✗ INVALID");
        chainStatusLabel.setStyle(isValid ? 
            "-fx-text-fill: #27ae60; -fx-font-weight: bold;" :
            "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        Block lastBlock = blockchain.getLastBlock();
        lastUpdateLabel.setText(dateFormat.format(new Date(lastBlock.timestamp())));
    }

    private void setupButtons() {
        backBtn.setOnAction(event -> {
            try { 
                App.loadMainMenu(); 
            } catch (IOException e) { 
                showError("Erreur", "Impossible de retourner au menu"); 
            }
        });

        refreshBtn.setOnAction(event -> {
            System.out.println("\n🔄 REFRESH Manuel demandé");
            
            // Recharger depuis SharedDataManager
            blockchain = sharedData.getBlockchain();
            loadBlockchain();
            
            showInfo("Rafraîchi", 
                "✅ Blockchain rechargée!\n\n" +
                "Total blocs: " + blockchain.getSize() + "\n" +
                "Affichés dans le tableau: " + tableData.size());
        });

        verifyBtn.setOnAction(event -> {
            boolean valid = blockchain.isChainValid();
            if (valid) {
                showInfo("✓ Blockchain Valide", 
                    "✅ Tous les blocs sont valides.\n\n" +
                    "Total blocs: " + blockchain.getSize() + "\n" +
                    "Intégrité vérifiée avec succès.");
            } else {
                showError("✗ Blockchain Invalide", 
                    "❌ La blockchain a été compromise!\n\n" +
                    "Des modifications non autorisées ont été détectées.");
            }
            updateInfoPanel();
        });

        exportBtn.setOnAction(event -> exportToCSV());
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la Blockchain");
        fileChooser.setInitialFileName("blockchain_export_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(Block.getCSVHeader() + "\n");
                for (Block block : blockchain.getChain()) {
                    writer.write(block.toCSV() + "\n");
                }
                showInfo("Export Réussi", 
                    "✅ Blockchain exportée avec succès!\n\n" +
                    "Fichier: " + file.getName() + "\n" +
                    "Emplacement: " + file.getParent() + "\n" +
                    "Nombre de blocs: " + blockchain.getSize());
            } catch (IOException e) {
                showError("Erreur d'Export", 
                    "Impossible d'exporter la blockchain:\n" + e.getMessage());
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