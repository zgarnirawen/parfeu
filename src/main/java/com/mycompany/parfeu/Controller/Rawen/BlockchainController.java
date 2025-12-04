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
 * 🔥 VERSION CORRIGÉE - Affichage des décisions en texte
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
    @FXML private TableColumn<BlockchainTableData, String> colDecisions;  // 🔥 MODIFIÉ : String au lieu de Integer
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
        
        // Récupère la blockchain partagée
        sharedData = SharedDataManager.getInstance();
        blockchain = sharedData.getBlockchain();
        tableData = FXCollections.observableArrayList();

        // 🔥 ORDRE CRITIQUE : D'abord configurer, PUIS charger
        setupTableColumns();
        loadBlockchain();
        setupButtons();
        
        System.out.println("✅ BlockchainController initialisé");
        System.out.println("   Blocs dans la blockchain: " + blockchain.getSize());
        System.out.println("   Lignes dans le tableau: " + tableData.size());
        System.out.println("================================================\n");
    }

    /**
     * 🔥 CONFIGURATION CRITIQUE DES COLONNES AVEC DECISIONS EN TEXTE
     */
    private void setupTableColumns() {
        System.out.println("🔧 Configuration des colonnes...");
        
        // 🔥 VÉRIFIER QUE LES COLONNES EXISTENT
        if (colBlockIndex == null) {
            System.err.println("✗ ERREUR: colBlockIndex est NULL!");
            return;
        }
        
        // Configuration EXPLICITE de chaque colonne
        colBlockIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colSrcIP.setCellValueFactory(new PropertyValueFactory<>("srcIP"));
        colDestIP.setCellValueFactory(new PropertyValueFactory<>("destIP"));
        colProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        
        // 🔥 MODIFIÉ : Utiliser "decisions" au lieu de "decisionsCount"
        colDecisions.setCellValueFactory(new PropertyValueFactory<>("decisions"));
        
        colHash.setCellValueFactory(new PropertyValueFactory<>("hashShort"));

        System.out.println("  ✓ CellValueFactory configurées");

        // 🔥 STYLE DU TABLEAU - Texte NOIR visible
        if (blockchainTable != null) {
            blockchainTable.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: black !important;" +
                "-fx-font-size: 13px;"
            );
            
            // Lier les données
            blockchainTable.setItems(tableData);
            System.out.println("  ✓ Données liées au tableau");
        }

        // 🔥 FACTORY POUR LES LIGNES - Couleurs avec texte noir
        blockchainTable.setRowFactory(tv -> new TableRow<BlockchainTableData>() {
            @Override
            protected void updateItem(BlockchainTableData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                    setText("");
                } else {
                    // Texte NOIR sur tous les fonds
                    if (item.isGenesis()) {
                        // Genesis - vert pâle
                        setStyle("-fx-background-color: #C8E6C9; -fx-text-fill: #000000; -fx-font-weight: bold;");
                    } else if (item.getDecisions().contains("DROP")) {
                        // Paquets bloqués - rouge pâle
                        setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #000000;");
                    } else if (item.getDecisions().contains("ALERT")) {
                        // Alertes - orange pâle
                        setStyle("-fx-background-color: #FFE0B2; -fx-text-fill: #000000;");
                    } else if (item.getDecisions().contains("ACCEPT")) {
                        // Acceptés - vert très pâle
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #000000;");
                    } else {
                        // Normal - blanc
                        setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
                    }
                }
            }
        });
        
        // 🔥 FORCER LE STYLE DES CELLULES
        colBlockIndex.setStyle("-fx-text-fill: black;");
        colTimestamp.setStyle("-fx-text-fill: black;");
        colSrcIP.setStyle("-fx-text-fill: black;");
        colDestIP.setStyle("-fx-text-fill: black;");
        colProtocol.setStyle("-fx-text-fill: black;");
        colDecisions.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");  // 🔥 Bold pour les décisions
        colHash.setStyle("-fx-text-fill: black;");
        
        System.out.println("  ✓ Styles appliqués (texte noir)");
    }

    /**
     * 🔥 CHARGEMENT DES DONNÉES
     */
    private void loadBlockchain() {
        System.out.println("\n📦 Chargement de la blockchain...");
        
        tableData.clear();
        List<Block> chain = blockchain.getChain();
        
        System.out.println("   Nombre de blocs: " + chain.size());
        
        if (chain.isEmpty()) {
            System.out.println("   ⚠️ Blockchain vide!");
            return;
        }
        
        // Ajouter chaque bloc au tableau
        for (int i = 0; i < chain.size(); i++) {
            Block block = chain.get(i);
            BlockchainTableData data = new BlockchainTableData(block);
            tableData.add(data);
            
            System.out.println("   ✓ Bloc #" + block.index() + 
                             " | " + data.getSrcIP() + 
                             " -> " + data.getDestIP() + 
                             " | " + data.getProtocol() +
                             " | Decisions: " + data.getDecisions());  // 🔥 NOUVEAU
        }
        
        System.out.println("   📊 Total dans tableData: " + tableData.size());
        
        // Forcer le rafraîchissement
        if (blockchainTable != null) {
            blockchainTable.refresh();
            System.out.println("   🔄 Tableau rafraîchi");
        }
        
        // Mettre à jour le panneau d'info
        updateInfoPanel();
        
        // 🔥 DEBUG FINAL
        System.out.println("\n🔍 VÉRIFICATION FINALE:");
        System.out.println("   - Blockchain size: " + blockchain.getSize());
        System.out.println("   - TableData size: " + tableData.size());
        System.out.println("   - Table items: " + (blockchainTable.getItems() != null ? blockchainTable.getItems().size() : "NULL"));
        
        if (tableData.isEmpty()) {
            System.err.println("   ✗ PROBLÈME: tableData est VIDE!");
        } else {
            System.out.println("   ✓ Données chargées correctement");
        }
    }

    /**
     * Met à jour le panneau d'information
     */
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

    /**
     * Configure les boutons
     */
    private void setupButtons() {
        backBtn.setOnAction(event -> {
            try { 
                App.loadMainMenu(); 
            } catch (IOException e) { 
                showError("Erreur", "Impossible de retourner au menu"); 
            }
        });

        refreshBtn.setOnAction(event -> {
            System.out.println("\n🔄 REFRESH Manuel");
            // Recharger la blockchain
            blockchain = sharedData.getBlockchain();
            loadBlockchain();
            showInfo("Rafraîchi", 
                "Blockchain rechargée!\n\n" +
                "Total blocs: " + blockchain.getSize() + "\n" +
                "Affichés: " + tableData.size());
        });

        verifyBtn.setOnAction(event -> {
            boolean valid = blockchain.isChainValid();
            if (valid) {
                showInfo("✓ Blockchain Valide", 
                    "Tous les blocs sont valides.\n\n" +
                    "Total blocs: " + blockchain.getSize());
            } else {
                showError("✗ Blockchain Invalide", 
                    "La blockchain a été compromise!");
            }
            updateInfoPanel();
        });

        exportBtn.setOnAction(event -> exportToCSV());
    }

    /**
     * Export CSV
     */
    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la Blockchain");
        fileChooser.setInitialFileName("blockchain_export_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV", "*.csv"));

        File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(Block.getCSVHeader() + "\n");
                for (Block block : blockchain.getChain()) {
                    writer.write(block.toCSV() + "\n");
                }
                showInfo("Export Réussi", 
                    "Blockchain exportée!\n\n" +
                    "Fichier: " + file.getName() + "\n" +
                    "Blocs: " + blockchain.getSize());
            } catch (IOException e) {
                showError("Erreur", "Impossible d'exporter: " + e.getMessage());
            }
        }
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public BlockChain getBlockchain() { 
        return blockchain; 
    }
}