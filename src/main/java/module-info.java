module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;



    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
    exports com.cgvsu.sceneview;
    opens com.cgvsu.sceneview to javafx.fxml;
}