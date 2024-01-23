module assign4 {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    opens assign4 to javafx.fxml;
    exports assign4;
}
