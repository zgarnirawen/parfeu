package com.mycompany.parfeu.Controller.Rawen;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;

import java.net.URL;
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
    @FXML private TableView<String> statsTable;
    @FXML private TableColumn<String, String> colProtocol;
    @FXML private TableColumn<String, String> colPackets;
    @FXML private TextArea detailsTextArea;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStatistics();
        setupCharts();
        System.out.println("✓ StatisticsController initialisé");
    }

    private void loadStatistics() {
        // Exemple de données (à remplacer par des vraies statistiques)
        totalPacketsLabel.setText("0");
        acceptedLabel.setText("0");
        blockedLabel.setText("0");
        alertedLabel.setText("0");
        
        detailsTextArea.setText("Aucune statistique disponible.\n\nCommencez par analyser des paquets.");
    }

    private void setupCharts() {
        // Configuration du PieChart (exemple)
        pieChart.getData().clear();
        pieChart.getData().add(new PieChart.Data("Acceptés", 0));
        pieChart.getData().add(new PieChart.Data("Bloqués", 0));
        pieChart.getData().add(new PieChart.Data("Alertes", 0));
    }
}