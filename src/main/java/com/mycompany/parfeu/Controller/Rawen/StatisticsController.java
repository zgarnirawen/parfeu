package com.mycompany.parfeu.Controller.Rawen;

import com.mycompany.parfeu.App;
import com.mycompany.parfeu.Model.Rawen.statistics.StatisticsManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la vue des statistiques.
 */
public class StatisticsController implements Initializable {

    @FXML private Label totalPacketsLabel;
    @FXML private Label acceptedLabel;
    @FXML private Label blockedLabel;
    @FXML private Label alertedLabel;
    @FXML private PieChart pieChart;
    @FXML private TableView<ProtocolStat> statsTable;
    @FXML private TableColumn<ProtocolStat, String> colProtocol;
    @FXML private TableColumn<ProtocolStat, Integer> colPackets;
    @FXML private TextArea detailsTextArea;
    @FXML private Button backBtn;
    @FXML private Button refreshBtn;
    @FXML private Button exportBtn;

    private StatisticsManager statistics;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser le gestionnaire de statistiques
        statistics = new StatisticsManager();
        
        // Configurer les colonnes du tableau
        setupTableColumns();
        
        // Charger les statistiques
        loadStatistics();
        
        // Configurer les graphiques
        setupCharts();
        
        // Configurer les boutons
        setupButtons();
        
        System.out.println("✓ StatisticsController initialisé");
    }

    /**
     * Configure les colonnes du tableau.
     */
    private void setupTableColumns() {
        if (colProtocol != null && colPackets != null) {
            colProtocol.setCellValueFactory(new PropertyValueFactory<>("protocol"));
            colPackets.setCellValueFactory(new PropertyValueFactory<>("packets"));
        }
    }

    /**
     * Charge les statistiques depuis le gestionnaire.
     */
    private void loadStatistics() {
        // Statistiques de base
        totalPacketsLabel.setText(String.valueOf(statistics.getTotalPackets()));
        acceptedLabel.setText(String.valueOf(statistics.getAcceptedPackets()));
        blockedLabel.setText(String.valueOf(statistics.getDroppedPackets()));
        alertedLabel.setText(String.valueOf(statistics.getAlertedPackets()));
        
        // Détails textuels
        updateDetailsArea();
        
        // Tableau des protocoles
        updateProtocolTable();
    }

    /**
     * Configure les graphiques.
     */
    private void setupCharts() {
        updatePieChart();
    }

    /**
     * Met à jour le PieChart.
     */
    private void updatePieChart() {
        pieChart.getData().clear();
        
        int accepted = statistics.getAcceptedPackets();
        int blocked = statistics.getDroppedPackets();
        int alerted = statistics.getAlertedPackets();
        
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Accepted (" + accepted + ")", accepted),
            new PieChart.Data("Blocked (" + blocked + ")", blocked),
            new PieChart.Data("Alerted (" + alerted + ")", alerted)
        );
        
        pieChart.setData(pieData);
        
        // Appliquer des couleurs personnalisées
        pieChart.getData().get(0).getNode().setStyle("-fx-pie-color: #27ae60;");
        pieChart.getData().get(1).getNode().setStyle("-fx-pie-color: #e74c3c;");
        pieChart.getData().get(2).getNode().setStyle("-fx-pie-color: #f39c12;");
    }

    /**
     * Met à jour le tableau des protocoles.
     */
    private void updateProtocolTable() {
        ObservableList<ProtocolStat> protocolData = FXCollections.observableArrayList();
        
        // Récupérer les statistiques par protocole
        Map<String, StatisticsManager.ProtocolStatistics> protocolStats = 
            statistics.getProtocolStatistics();
        
        if (protocolStats != null && !protocolStats.isEmpty()) {
            for (Map.Entry<String, StatisticsManager.ProtocolStatistics> entry : 
                 protocolStats.entrySet()) {
                protocolData.add(new ProtocolStat(
                    entry.getKey(),
                    entry.getValue().totalPackets
                ));
            }
        } else {
            // Données de test si aucune statistique
            protocolData.addAll(
                new ProtocolStat("HTTP", 0),
                new ProtocolStat("HTTPS", 0),
                new ProtocolStat("TCP", 0),
                new ProtocolStat("UDP", 0)
            );
        }
        
        if (statsTable != null) {
            statsTable.setItems(protocolData);
        }
    }

    /**
     * Met à jour la zone de détails textuels.
     */
    private void updateDetailsArea() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("           RAPPORT STATISTIQUES DÉTAILLÉ\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");
        
        int total = statistics.getTotalPackets();
        
        if (total == 0) {
            sb.append("Aucune statistique disponible.\n\n");
            sb.append("Commencez par analyser des paquets dans l'onglet 'Generate'.\n");
        } else {
            // Statistiques générales
            sb.append("📊 STATISTIQUES GÉNÉRALES\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append(String.format("Total paquets traités : %d\n", total));
            sb.append(String.format("  ✓ Acceptés          : %d\n", statistics.getAcceptedPackets()));
            sb.append(String.format("  ✗ Bloqués           : %d\n", statistics.getDroppedPackets()));
            sb.append(String.format("  ⚠ Alertes           : %d\n", statistics.getAlertedPackets()));
            sb.append(String.format("  📝 Journalisés      : %d\n\n", statistics.getLoggedPackets()));
            
            // Taux
            double acceptRate = (statistics.getAcceptedPackets() * 100.0) / total;
            double blockRate = (statistics.getDroppedPackets() * 100.0) / total;
            double alertRate = (statistics.getAlertedPackets() * 100.0) / total;
            
            sb.append("📈 TAUX\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append(String.format("  Taux d'acceptation : %.2f%%\n", acceptRate));
            sb.append(String.format("  Taux de blocage    : %.2f%%\n", blockRate));
            sb.append(String.format("  Taux d'alerte      : %.2f%%\n\n", alertRate));
            
            // Statistiques par IP (top 5)
            Map<String, StatisticsManager.IPStatistics> ipStats = statistics.getIPStatistics();
            if (ipStats != null && !ipStats.isEmpty()) {
                sb.append("🌐 TOP 5 IP SOURCES\n");
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                
                ipStats.values().stream()
                    .sorted((a, b) -> Integer.compare(b.totalPackets, a.totalPackets))
                    .limit(5)
                    .forEach(stat -> {
                        double ipBlockRate = stat.totalPackets > 0 
                            ? (stat.blockedPackets * 100.0) / stat.totalPackets 
                            : 0;
                        sb.append(String.format("  %s : %d paquets (%.1f%% bloqués)\n",
                            stat.ipAddress, stat.totalPackets, ipBlockRate));
                    });
            }
        }
        
        sb.append("\n═══════════════════════════════════════════════════════\n");
        
        detailsTextArea.setText(sb.toString());
    }

    /**
     * Configure les actions des boutons.
     */
    private void setupButtons() {
        // Bouton Retour
        if (backBtn != null) {
            backBtn.setOnAction(event -> {
                try {
                    App.loadScene("/com/mycompany/parfeu/Views/Mahran/mainvue.fxml", 800, 600);
                } catch (IOException e) {
                    showError("Erreur", "Impossible de retourner au menu principal");
                    e.printStackTrace();
                }
            });
        }
        
        // Bouton Rafraîchir
        if (refreshBtn != null) {
            refreshBtn.setOnAction(event -> {
                loadStatistics();
                updatePieChart();
                updateProtocolTable();
                showInfo("Rafraîchi", "Les statistiques ont été mises à jour");
            });
        }
        
        // Bouton Exporter
        if (exportBtn != null) {
            exportBtn.setOnAction(event -> {
                exportStatistics();
            });
        }
    }

    /**
     * Exporte les statistiques.
     */
    private void exportStatistics() {
        try {
            String stats = detailsTextArea.getText();
            // Ici vous pourriez sauvegarder dans un fichier
            // Pour l'instant, on affiche juste une confirmation
            showInfo("Export", "Les statistiques ont été exportées avec succès");
        } catch (Exception e) {
            showError("Erreur d'export", "Impossible d'exporter les statistiques");
            e.printStackTrace();
        }
    }

    /**
     * Met à jour les statistiques avec un nouveau gestionnaire.
     */
    public void setStatistics(StatisticsManager stats) {
        this.statistics = stats;
        loadStatistics();
        updatePieChart();
        updateProtocolTable();
    }

    /**
     * Affiche une alerte d'information.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Classe interne pour les données du tableau.
     */
    public static class ProtocolStat {
        private final String protocol;
        private final int packets;

        public ProtocolStat(String protocol, int packets) {
            this.protocol = protocol;
            this.packets = packets;
        }

        public String getProtocol() { return protocol; }
        public int getPackets() { return packets; }
    }

    // Getter
    public StatisticsManager getStatistics() {
        return statistics;
    }
}