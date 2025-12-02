package com.mycompany.parfeu.Controller.Rawen;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la vue Blockchain (historique).
 */
public class BlockchainController implements Initializable {

    @FXML
    private VBox blocksContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadBlockchain();
        System.out.println("✓ BlockchainController initialisé");
    }

    private void loadBlockchain() {
        // TODO: Charger les blocs de la blockchain
        // Pour l'instant, afficher un message par défaut
    }
}