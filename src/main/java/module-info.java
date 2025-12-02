module com.mycompany.parfeu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.parfeu to javafx.fxml;
    exports com.mycompany.parfeu;
}
