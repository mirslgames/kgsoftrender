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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import java.util.*;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;


import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;

import static java.nio.file.Path.*;

public class GuiController {

    final private float TRANSLATION = 5F;
    final private float ROT = 0.5F;
    private float pastX = 0;
    private float pastY = 0;

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
    public static Dictionary<String, Button> cacheNameSceneModelButtons = new Hashtable<>();
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
    @FXML
    private MenuBar menuBar;
    @FXML
    private Button applyTransformButton;
    @FXML
    private Label lightIntensityLabel;
    @FXML
    private Slider lightIntensitySlider;

    private Timeline timeline;

    @FXML
    private void initialize() {

        lightIntensityLabel.textProperty().bind(
                lightIntensitySlider.valueProperty().asString("%.2f")
        );

        lightIntensitySlider.valueProperty().addListener((obs, oldV, newV) -> {
            SceneManager.lightIntensity = newV.floatValue();
        });

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

        canvasParentAnchorPane.setOnMouseDragged(this::changeCameraPosition);
        canvasParentAnchorPane.setOnMousePressed(this::setPastXY);

        Platform.runLater(() -> {
            ThemeSettings.setLightTheme();
            applyTheme();
        });
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

        var gc = sceneCanvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.web(ThemeSettings.canvasBackgroundColor));
        gc.fillRect(0, 0, width, height);
        SceneManager.activeCamera.setAspectRatio((float) (width / height));


        for (Model model : SceneManager.models) {
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
    private void onLightThemeMenuItemClick() {
        ThemeSettings.setLightTheme();
        applyTheme();
    }

    @FXML
    private void onDarkThemeMenuItemClick() {
        ThemeSettings.setDarkTheme();
        applyTheme();
    }

    private void applyTheme() {
        Scene scene = sceneCanvas.getScene();
        if (scene == null) return;

        Parent root = scene.getRoot();
        root.setStyle(ThemeSettings.rootStyle);

        applyStyle(root, ".split-pane", ThemeSettings.splitPaneStyle);
        applyStyle(root, ".split-pane-divider", ThemeSettings.splitDividerStyle);

        applyStyle(root, ".anchor-pane", ThemeSettings.paneStyle);
        applyStyle(root, ".vbox", ThemeSettings.paneStyle);
        applyStyle(root, ".hbox", ThemeSettings.paneStyle);

        applyStyle(root, ".menu-bar", ThemeSettings.menuBarStyle);

        applyStyle(root, ".label", ThemeSettings.labelStyle);
        applyStyle(root, ".check-box", ThemeSettings.checkBoxStyle);
        applyStyle(root, ".text-field", ThemeSettings.textFieldStyle);

        applyStyle(root, ".button", ThemeSettings.buttonStyle);
        if (activeButton != null) {
            activeButton.setStyle(ThemeSettings.activeButtonStyle);
        }

        applyStyle(root, ".titled-pane > .title", ThemeSettings.titledPaneTitleStyle);
        applyStyle(root, ".titled-pane > .title > .text", ThemeSettings.titledPaneTitleTextStyle);
        applyStyle(root, ".titled-pane > *.content", ThemeSettings.titledPaneContentStyle);

        applyStyle(root, ".scroll-pane", ThemeSettings.scrollPaneStyle);
        applyStyle(root, ".scroll-pane .viewport", ThemeSettings.scrollPaneViewportStyle);

        applyStyle(root, ".scroll-bar", ThemeSettings.scrollBarStyle);
        applyStyle(root, ".scroll-bar .thumb", ThemeSettings.scrollBarThumbStyle);

        Platform.runLater(() -> applyStyle(root, ".menu-bar .label", ThemeSettings.menuBarLabelStyle));
    }

    private void applyStyle(Node root, String selector, String style) {
        Set<Node> nodes = root.lookupAll(selector);
        for (Node n : nodes) {
            n.setStyle(style);
        }
    }

    @FXML
    private void onPositionXChanged() {
        if(SceneManager.activeModel != null){
            SceneManager.activeModel.positionXValue = parseFloat(positionXTextField);
        }

    }

    @FXML
    private void onPositionYChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.positionYValue = parseFloat(positionYTextField);
        }
    }

    @FXML
    private void onPositionZChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.positionZValue = parseFloat(positionZTextField);
        }
    }

    @FXML
    private void onRotationXChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.rotationXValue = parseFloat(rotationXTextField);
        }
    }

    @FXML
    private void onRotationYChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.rotationYValue = parseFloat(rotationYTextField);
        }
    }

    @FXML
    private void onRotationZChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.rotationZValue = parseFloat(rotationZTextField);
        }
    }

    @FXML
    private void onScaleXChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.scaleXValue = parseFloat(scaleXTextField);
        }
    }

    @FXML
    private void onScaleYChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.scaleYValue = parseFloat(scaleYTextField);
        }
    }

    @FXML
    private void onScaleZChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.scaleZValue = parseFloat(scaleZTextField);
        }
    }

    @FXML
    private void onApplyTransformButtonClick(){
        //Для теста ручного
        /*String text = SceneManager.activeModel.positionXValue + "\n" +
                SceneManager.activeModel.positionYValue + "\n" +
                SceneManager.activeModel.positionZValue + "\n" +
                SceneManager.activeModel.rotationXValue + "\n" +
                SceneManager.activeModel.rotationYValue + "\n" +
                SceneManager.activeModel.rotationZValue + "\n" +
                SceneManager.activeModel.scaleXValue + "\n" +
                SceneManager.activeModel.scaleYValue + "\n" +
                SceneManager.activeModel.scaleZValue + "\n";

        showWarning(text);*/
    }

    @FXML
    private void onSaveModelMenuItemClick() {

        if(SceneManager.activeModel == null){
            showWarning("Выберите конкретную модель");
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
            fileChooser.setTitle("Save Model");

            File file = fileChooser.showSaveDialog((Stage) sceneCanvas.getScene().getWindow());
            Path fileName = Path.of(file.getAbsolutePath());
            ObjWriter.writeModelToFile(SceneManager.activeModel, fileName.toString());
        }
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
            validateAndCorrectDuplicateModelName(mesh);

            //Добавление кнопки
            addModelButton(mesh);
            // todo: обработка ошибок
        } catch (IOException exception) {

        }
    }

    protected static void validateAndCorrectDuplicateModelName(Model targetModel){
        if (SceneManager.historyModelName.containsKey(targetModel.modelName)){
            int c = SceneManager.historyModelName.get(targetModel.modelName);
            SceneManager.historyModelName.put(targetModel.modelName, ++c);
            targetModel.modelName += String.format(" (%d)", c);
        } else{
            SceneManager.historyModelName.put(targetModel.modelName, 0);
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
        String deletedModelName = SceneManager.activeModel.modelName;
        boolean result = SceneManager.removeModelFromScene(SceneManager.activeModel);
        if (result){
            Button currentDelButton = cacheNameSceneModelButtons.get(deletedModelName);

            cacheNameSceneModelButtons.remove(deletedModelName);
            modelButtons.remove(currentDelButton);
            modelsBox.getChildren().remove(currentDelButton);
            if(activeButton.getText().equals(deletedModelName)){
                activeButton = null;
            }

            deleteActiveEntityButton.setVisible(false);
            transformationTitledPane.setVisible(false);
        }


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
            activeButton.setStyle(ThemeSettings.buttonStyle);
        }
        activeButton = button;
        activeButton.setStyle(ThemeSettings.activeButtonStyle);

        setTextFieldModelTransform(model);

        deleteActiveEntityButton.setVisible(true);
        transformationTitledPane.setVisible(true);
    }

    private void setTextFieldModelTransform(Model model){
        positionXTextField.setText(""+model.positionXValue);
        positionYTextField.setText(""+model.positionYValue);
        positionZTextField.setText(""+model.positionZValue);
        rotationXTextField.setText(""+model.rotationXValue);
        rotationYTextField.setText(""+model.rotationYValue);
        rotationZTextField.setText(""+model.rotationZValue);
        scaleXTextField.setText(""+model.scaleXValue);
        scaleYTextField.setText(""+model.scaleYValue);
        scaleZTextField.setText(""+model.scaleZValue);

    }

    private void addModelButton(Model model) {
        SceneManager.loadModelToScene(model);

        Button btn = new Button(model.modelName);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(this::onModelButtonClick);

        modelButtons.add(btn);
        cacheNameSceneModelButtons.put(model.modelName, btn);
        btn.setStyle(ThemeSettings.buttonStyle);

        modelsBox.getChildren().add(btn);

    }

    @FXML
    public void changeCameraPosition(MouseEvent mouseEvent) {

        float deltaX = (float) (mouseEvent.getX() - pastX);
        float deltaY = (float) (mouseEvent.getY() - pastY);
        pastX = (float) mouseEvent.getX();
        pastY = (float) mouseEvent.getY();


        SceneManager.activeCamera.rotateCamera(-deltaX * ROT, -deltaY * ROT);
    }

    public void setPastXY(MouseEvent mouseEvent) {
        pastX = (float) mouseEvent.getX();
        pastY = (float) mouseEvent.getY();
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