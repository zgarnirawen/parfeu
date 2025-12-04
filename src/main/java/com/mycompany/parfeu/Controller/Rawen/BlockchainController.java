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
        // Récupère la blockchain partagée
        sharedData = SharedDataManager.getInstance();
        blockchain = sharedData.getBlockchain();
        tableData = FXCollections.observableArrayList();

        setupTable();
        loadBlockchain();
        setupButtons();
    }

    /**
     * Configure la TableView avec les colonnes et le style des lignes.
     */
    private void setupTable() {
        colBlockIndex.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("index"));
        colTimestamp.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
        colSrcIP.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("srcIP"));
        colDestIP.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("destIP"));
        colProtocol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("protocol"));
        colDecisions.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("decisionsCount"));
        colHash.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hashShort"));

        blockchainTable.setItems(tableData);

        // Style des lignes selon le type de bloc
        blockchainTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(BlockchainTableData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getIndex() == 0) {
                    setStyle("-fx-background-color: #d4edda;"); // Genesis
                } else if (item.getDecisionsCount() == 0) {
                    setStyle("-fx-background-color: #f8f9fa;"); // Vide
                } else {
                    setStyle("-fx-background-color: white;"); // Normal
                }
            }
        });
    }

    /**
     * Charge la blockchain dans le TableView.
     */
    private void loadBlockchain() {
        tableData.clear();
        List<Block> chain = blockchain.getChain();
        for (Block block : chain) {
            tableData.add(new BlockchainTableData(block));
        }
        updateInfoPanel();
        blockchainTable.refresh();
    }

    /**
     * Met à jour le panneau d'information.
     */
    private void updateInfoPanel() {
        totalBlocksLabel.setText(String.valueOf(blockchain.getSize()));

        boolean isValid = blockchain.isChainValid();
        chainStatusLabel.setText(isValid ? "✓ VALID" : "✗ INVALID");
        chainStatusLabel.setStyle(isValid ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                                          : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        Block lastBlock = blockchain.getLastBlock();
        lastUpdateLabel.setText(dateFormat.format(new Date(lastBlock.timestamp())));
    }

    /**
     * Configure les actions des boutons.
     */
    private void setupButtons() {
        backBtn.setOnAction(event -> {
            try { App.loadMainMenu(); }
            catch (IOException e) { showError("Erreur", "Impossible de retourner au menu."); }
        });

        refreshBtn.setOnAction(event -> {
            loadBlockchain();
            showInfo("Rafraîchi", "Blockchain rechargée avec succès !");
        });

        verifyBtn.setOnAction(event -> {
            boolean valid = blockchain.isChainValid();
            if (valid) {
                showInfo("✓ Blockchain Valide", "Tous les blocs sont valides et la chaîne est intacte.");
            } else {
                showError("✗ Blockchain Invalide", "La blockchain a été compromise !");
            }
            updateInfoPanel();
        });

        exportBtn.setOnAction(event -> exportToCSV());
    }

    /**
     * Exporte la blockchain vers un fichier CSV.
     */
    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter la Blockchain");
        fileChooser.setInitialFileName("blockchain_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));

        File file = fileChooser.showSaveDialog(exportBtn.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(Block.getCSVHeader() + "\n");
                for (Block block : blockchain.getChain()) {
                    writer.write(block.toCSV() + "\n");
                }
                showInfo("Export Réussi", "Blockchain exportée avec succès !");
            } catch (IOException e) {
                showError("Erreur", "Impossible d'exporter : " + e.getMessage());
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

    public BlockChain getBlockchain() { return blockchain; }
}
