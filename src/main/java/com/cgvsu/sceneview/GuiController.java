package com.cgvsu.sceneview;

import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.service.ShortcutsSettings;
import com.cgvsu.service.ThemeSettings;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import javax.vecmath.Vector3f;


import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane canvasParentAnchorPane;
    @FXML
    private Canvas sceneCanvas;
    @FXML
    private CheckBox drawMeshCheckBox;
    @FXML
    private CheckBox useTextureCheckBox;
    @FXML
    private CheckBox useLightCheckBox;
    @FXML
    private VBox modelsBox;
    @FXML
    private Button deleteActiveEntityButton;
    @FXML
    private TitledPane transformationTitledPane;

    private final ArrayList<Button> modelButtons = new ArrayList<>();
    private Button activeButton;

    @FXML
    private TextField positionXTextField;
    @FXML
    private TextField positionYTextField;
    @FXML
    private TextField positionZTextField;
    @FXML
    private TextField rotationXTextField;
    @FXML
    private TextField rotationYTextField;
    @FXML
    private TextField rotationZTextField;
    @FXML
    private TextField scaleXTextField;
    @FXML
    private TextField scaleYTextField;
    @FXML
    private TextField scaleZTextField;
    @FXML
    private MenuItem saveModeMenuItem;
    @FXML
    private MenuItem openModeMenuItem;



    private Timeline timeline;


    @FXML
    private void initialize() {
        positionXTextField.setOnKeyReleased(e -> onPositionXChanged());
        positionYTextField.setOnKeyReleased(e -> onPositionYChanged());
        positionZTextField.setOnKeyReleased(e -> onPositionZChanged());

        rotationXTextField.setOnKeyReleased(e -> onRotationXChanged());
        rotationYTextField.setOnKeyReleased(e -> onRotationYChanged());
        rotationZTextField.setOnKeyReleased(e -> onRotationZChanged());

        scaleXTextField.setOnKeyReleased(e -> onScaleXChanged());
        scaleYTextField.setOnKeyReleased(e -> onScaleYChanged());
        scaleZTextField.setOnKeyReleased(e -> onScaleZChanged());

        openModeMenuItem.setAccelerator(KeyCombination.keyCombination(ShortcutsSettings.openModel));
        saveModeMenuItem.setAccelerator(KeyCombination.keyCombination(ShortcutsSettings.saveModel));

        ThemeSettings.setDefaultTheme();
        SceneManager.initialize();

        sceneCanvas.setFocusTraversable(true);
        sceneCanvas.setOnMouseClicked(e -> sceneCanvas.requestFocus());

        sceneCanvas.widthProperty().bind(canvasParentAnchorPane.widthProperty());
        sceneCanvas.heightProperty().bind(canvasParentAnchorPane.heightProperty());
        deleteActiveEntityButton.setVisible(false);
        transformationTitledPane.setVisible(false);


        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), e -> renderFrame());


        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private void renderFrame() {
        double width = sceneCanvas.getWidth();
        double height = sceneCanvas.getHeight();

        sceneCanvas.getGraphicsContext2D().clearRect(0, 0, width, height);
        SceneManager.activeCamera.setAspectRatio((float) (width / height));


        for(Model model : SceneManager.models){
            RenderEngine.render(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, model, (int) width, (int) height);
        }
        //ВАРИАНТ рендерить только активную модель
        /*if (SceneManager.activeModel != null) {
            RenderEngine.render(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, SceneManager.activeModel, (int) width, (int) height);
        }*/
    }

    private float parseFloat(TextField textField) {
        String s = textField.getText();
        s = s.trim().replace(',', '.');

        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            //todo:обработка ошибок
        }
        return 0;
    }

    @FXML
    private void onPositionXChanged() {
        SceneManager.positionXValue = parseFloat(positionXTextField);
    }

    @FXML
    private void onPositionYChanged() {
        SceneManager.positionYValue = parseFloat(positionYTextField);
    }

    @FXML
    private void onPositionZChanged() {
        SceneManager.positionZValue = parseFloat(positionZTextField);
    }

    @FXML
    private void onRotationXChanged() {
        SceneManager.rotationXValue = parseFloat(rotationXTextField);
    }

    @FXML
    private void onRotationYChanged() {
        SceneManager.rotationYValue = parseFloat(rotationYTextField);
    }

    @FXML
    private void onRotationZChanged() {
        SceneManager.rotationZValue = parseFloat(rotationZTextField);
    }

    @FXML
    private void onScaleXChanged() {
        SceneManager.scaleXValue = parseFloat(scaleXTextField);
    }

    @FXML
    private void onScaleYChanged() {
        SceneManager.scaleYValue = parseFloat(scaleYTextField);
    }

    @FXML
    private void onScaleZChanged() {
        SceneManager.scaleZValue = parseFloat(scaleZTextField);
    }

    @FXML
    private void onSaveModelMenuItemClick(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");

        File file = fileChooser.showSaveDialog((Stage) sceneCanvas.getScene().getWindow());
        Path fileName = Path.of(file.getAbsolutePath());
        ObjWriter.writeModelToFile(SceneManager.activeModel, fileName.toString());

    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) sceneCanvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model mesh = ObjReader.read(fileContent, fileName.getFileName().toString(), SceneManager.historyModelName);
            if(SceneManager.historyModelName.containsKey(mesh.modelName)){
                int c = SceneManager.historyModelName.get(mesh.modelName);
                SceneManager.historyModelName.put(mesh.modelName, ++c);
            } else{
                SceneManager.historyModelName.put(mesh.modelName, 0);
            }

            //Добавление кнопки
            addModel(mesh);
            SceneManager.activeModel = mesh;
            // todo: обработка ошибок
        } catch (IOException exception) {

        }
    }

    public void showWarning(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public boolean askConfirm(String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(null);
        alert.setContentText(text);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void showInfo(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    @FXML
    private void onDeleteActiveEntityButtonClick(ActionEvent event) {

        deleteActiveEntityButton.setVisible(false);
        transformationTitledPane.setVisible(false);
    }

    @FXML
    private void onModelButtonClick(ActionEvent event) {
        Button button = (Button) event.getSource();
        String modelName = button.getText();

        Model model = SceneManager.cacheNameSceneModels.get(modelName);
        if (model == null) {
            System.out.println("Модель с именем " + modelName + " не найдена");
            return;
        }

        SceneManager.activeModel = model;

        if (activeButton != null) {
            activeButton.setStyle("");
        }
        activeButton = button;
        activeButton.setStyle(ThemeSettings.activeButtonStyle);

        deleteActiveEntityButton.setVisible(true);
        transformationTitledPane.setVisible(true);
    }

    public void addModel(Model model) {
        SceneManager.loadModelToScene(model);

        Button btn = new Button(model.modelName);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(this::onModelButtonClick);
        modelsBox.getChildren().add(btn);
    }


    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        SceneManager.activeCamera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        SceneManager.activeCamera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        SceneManager.activeCamera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        SceneManager.activeCamera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        SceneManager.activeCamera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        SceneManager.activeCamera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }
}