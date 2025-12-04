module com.mycompany.parfeu {
    // Modules JavaFX requis
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.parfeu to javafx.fxml;
    opens com.mycompany.parfeu.Controller.Mahran to javafx.fxml;
    opens com.mycompany.parfeu.Controller.Rawen to javafx.fxml;
    
    opens com.mycompany.parfeu.Model.Rawen.blockchain to javafx.base;
    
    // Exporter le package principal
    exports com.mycompany.parfeu;
    exports com.mycompany.parfeu.Controller.Mahran;
    exports com.mycompany.parfeu.Controller.Rawen;
    
    exports com.mycompany.parfeu.Model.Mahran.config;
    exports com.mycompany.parfeu.Model.Mahran.generator;
    exports com.mycompany.parfeu.Model.Rawen.analyzer;
    exports com.mycompany.parfeu.Model.Rawen.decision;
    exports com.mycompany.parfeu.Model.Rawen.engine;
    exports com.mycompany.parfeu.Model.Rawen.blockchain;
}