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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.Path.*;

public class GuiController {

    private static final Logger log = LoggerFactory.getLogger(GuiController.class);
    final private float TRANSLATION = 5F;
    final private float ROT = 0.5F;
    private float pastRotateX = 0;
    private float pastRotateY = 0;

    private float pastMoveX = 0;
    private float pastMoveY = 0;

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
    @FXML
    private TextArea logTextArea;
    @FXML
    private Button loadTextureButton;
    @FXML
    private Label currentTextureLabel;
    @FXML
    private Button deleteTextureButton;
    @FXML
    private Button renderButton;
    @FXML
    private RadioMenuItem oneFrameMenuItem;
    @FXML
    private RadioMenuItem transformFrameMenuItem;
    @FXML
    private RadioMenuItem cameraFrameMenuItem;
    @FXML
    private RadioMenuItem cameraTransformFrameMenuItem;
    @FXML
    private RadioMenuItem everyFrameMenuItem;
    @FXML
    private AnchorPane logAnchorPane;

    private RenderMode currentRenderMode;

    private Timeline timeline;

    private enum SaveVariant {
        ORIGINAL("Исходная модель"),
        MODIFIED("Изменённая");

        final String title;
        SaveVariant(String title) { this.title = title; }

        @Override public String toString() { return title; }
    }

    @FXML
    private void initialize() {

        try {
            Image img = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/default_texture.png"))
            );
            Model.defaultTexture = img;
            currentRenderMode = RenderMode.ONE_FRAME;
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

            installNumericFloatFilter(positionXTextField, true);
            installNumericFloatFilter(positionYTextField, true);
            installNumericFloatFilter(positionZTextField, true);

            installNumericFloatFilter(rotationXTextField, true);
            installNumericFloatFilter(rotationYTextField, true);
            installNumericFloatFilter(rotationZTextField, true);

            installNumericFloatFilter(scaleXTextField, false);
            installNumericFloatFilter(scaleYTextField, false);
            installNumericFloatFilter(scaleZTextField, false);


            openModeMenuItem.setAccelerator(KeyCombination.keyCombination(ShortcutsSettings.openModel));
            saveModeMenuItem.setAccelerator(KeyCombination.keyCombination(ShortcutsSettings.saveModel));

            canvasParentAnchorPane.setOnMouseDragged(this::changeCameraPosition);
            canvasParentAnchorPane.setOnMousePressed(this::setPastXY);
            canvasParentAnchorPane.setOnScroll(this::setZoom);

            Platform.runLater(() -> {
                ThemeSettings.setLightTheme();
                applyTheme();

                Scene scene = sceneCanvas.getScene();
                if (scene != null) {
                    installHoverForAllButtons(scene.getRoot());
                }
            });
            SceneManager.initialize();

            sceneCanvas.setFocusTraversable(true);
            sceneCanvas.setOnMouseClicked(e -> sceneCanvas.requestFocus());

            sceneCanvas.widthProperty().bind(canvasParentAnchorPane.widthProperty());
            sceneCanvas.heightProperty().bind(canvasParentAnchorPane.heightProperty());
            deleteActiveEntityButton.setVisible(false);
            transformationTitledPane.setVisible(false);
            drawMeshCheckBox.setSelected(SceneManager.drawMesh);
            useTextureCheckBox.setSelected(SceneManager.useTexture);
            useLightCheckBox.setSelected(SceneManager.useLight);
            drawMeshCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                SceneManager.drawMesh = newVal;
            });

            useTextureCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                SceneManager.useTexture = newVal;
            });

            useLightCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                SceneManager.useLight = newVal;
            });
            timeline = new Timeline();
            timeline.setCycleCount(Animation.INDEFINITE);

            KeyFrame frame = new KeyFrame(Duration.millis(15), e -> {
                if (currentRenderMode == RenderMode.EVERY_FRAME) {
                    renderFrame();
                }
            });


            timeline.getKeyFrames().add(frame);
            timeline.play();
        }
        catch(Exception exception){
            logError("Ошибка инициализации сцены");
            showError("Ошибка инициализации сцены");
        }
    }

    private void renderFrame() {
        double width = sceneCanvas.getWidth();
        double height = sceneCanvas.getHeight();

        var gc = sceneCanvas.getGraphicsContext2D();
        gc.setFill(javafx.scene.paint.Color.web(ThemeSettings.canvasBackgroundColor));
        gc.fillRect(0, 0, width, height);
        SceneManager.activeCamera.setAspectRatio((float) (width / height));


        for (Model model : SceneManager.models) {
            RenderEngine.renderWithRenderingMods(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, model, (int) width, (int) height);
            //RenderEngine.render(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, model, (int) width, (int) height);
        }
        //ВАРИАНТ рендерить только активную модель
        /*if (SceneManager.activeModel != null) {
            RenderEngine.render(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, SceneManager.activeModel, (int) width, (int) height);
        }*/
    }

    private void installNumericFloatFilter(TextField tf, boolean allowNegative) {
        if (tf == null) return;

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            // Разрешаем пустую строку
            if (newText.isEmpty()) return change;

            // Промежуточные состояния
            if (allowNegative) {
                if (newText.equals("-") || newText.equals("-.") || newText.equals("-,"))
                    return change;
            }
            if (newText.equals(".") || newText.equals(",")) return change;

            //Полноценные числа с учетом знака
            String sign = allowNegative ? "-?" : "";
            String pattern = sign + "(?:\\d+(?:[\\.,]\\d*)?|[\\.,]\\d+)";

            if (newText.matches(pattern)) return change;

            return null;
        };

        tf.setTextFormatter(new TextFormatter<>(filter));
    }


    private float parseFloat(TextField textField) {
        String s = textField.getText();
        s = s.trim().replace(',', '.');
        if(s.isEmpty() || s.equals("-") || s.equals("-.") || s.isBlank() || s.endsWith(".") || s.endsWith(",")){
            return 0;
        }
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            logError(e.getMessage());
            showError(e.getMessage());

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
        try {
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

            logTextArea.setStyle(ThemeSettings.textAreaStyle);

            /*if (applyTransformButton != null) {
                applyTransformButton.setStyle(
                        applyTransformButton.isHover() ? ThemeSettings.buttonHoverStyle : ThemeSettings.buttonStyle
                );
            }*/
        }
        catch (Exception exception){
            logError("Ошибка при применении темы");
            showError("Ошибка при применении темы");
        }
    }

    private void appendLog(String level, String text) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String line = String.format("[%s] [%s] %s%n", time, level, text);

        Platform.runLater(() -> {
            logTextArea.appendText(line);


            logTextArea.positionCaret(logTextArea.getText().length());
        });
    }

    private void applyStyle(Node root, String selector, String style) {
        Set<Node> nodes = root.lookupAll(selector);
        for (Node n : nodes) {
            n.setStyle(style);
        }
    }

    @FXML private void oneFrameMenuItemClick(ActionEvent event){
        currentRenderMode = RenderMode.ONE_FRAME;
        renderButton.setDisable(false);
    }
    @FXML private void transformFrameMenuItemClick(ActionEvent event){
        currentRenderMode = RenderMode.EVERY_TRANSFORM_FRAME;
        renderButton.setDisable(true);
    }
    @FXML private void cameraFrameMenuItemClick(ActionEvent event){
        currentRenderMode = RenderMode.EVERY_CAMERA_MOTION_FRAME;
        renderButton.setDisable(true);
    }
    @FXML private void cameraTransformFrameMenuItemClick(ActionEvent event){
        currentRenderMode = RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME;
        renderButton.setDisable(true);
    }
    @FXML private void everyFrameMenuItemClick(ActionEvent event){
        currentRenderMode = RenderMode.EVERY_FRAME;
        renderButton.setDisable(true);
    }

    private Optional<SaveVariant> askSaveVariant() {
        ChoiceDialog<SaveVariant> dialog = new ChoiceDialog<>(
                SaveVariant.MODIFIED,
                List.of(SaveVariant.ORIGINAL, SaveVariant.MODIFIED)
        );
        dialog.setTitle("Сохранение модели");
        dialog.setHeaderText("Какую модель сохранить?");
        dialog.setContentText("Выберите вариант:");

        return dialog.showAndWait();
    }

    @FXML
    private void onPositionXChanged() {
        if(SceneManager.activeModel != null){
            SceneManager.activeModel.currentTransform.positionX = parseFloat(positionXTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }

    }

    @FXML
    private void onPositionYChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.positionY = parseFloat(positionYTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    @FXML
    private void onPositionZChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.positionZ = parseFloat(positionZTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    @FXML
    private void onRotationXChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.rotationX = parseFloat(rotationXTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    @FXML
    private void onRotationYChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.rotationY = parseFloat(rotationYTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    @FXML
    private void onRotationZChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.rotationZ = parseFloat(rotationZTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    @FXML
    private void onScaleXChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.scaleX = parseFloat(scaleXTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    @FXML
    private void onScaleYChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.scaleY = parseFloat(scaleYTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    @FXML
    private void onScaleZChanged() {
        if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.scaleZ = parseFloat(scaleZTextField);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }

    private void installHoverForButton(ButtonBase button) {
        if (button == null) return;

        button.setOnMouseEntered(e -> {
            if (button == activeButton) {
                button.setStyle(ThemeSettings.activeButtonStyle);
            } else {
                button.setStyle(ThemeSettings.buttonHoverStyle);
            }
        });

        button.setOnMouseExited(e -> {
            if (button == activeButton) {
                button.setStyle(ThemeSettings.activeButtonStyle);
            } else {
                button.setStyle(ThemeSettings.buttonStyle);
            }
        });
    }

    private void installHoverForAllButtons(Parent root) {
        if (root == null) return;

        Set<Node> nodes = root.lookupAll(".button");
        for (Node n : nodes) {
            if (n instanceof ButtonBase b) {
                installHoverForButton(b);

                if (b == activeButton) {
                    b.setStyle(ThemeSettings.activeButtonStyle);
                } else {
                    b.setStyle(b.isHover() ? ThemeSettings.buttonHoverStyle : ThemeSettings.buttonStyle);
                }
            }
        }
    }


    @FXML
    private void onApplyTransformButtonClick(){
        //Вариант если через кнопку применить
        /*if(SceneManager.activeModel != null) {
            SceneManager.activeModel.currentTransform.positionX = parseFloat(positionXTextField);
            SceneManager.activeModel.currentTransform.positionY = parseFloat(positionYTextField);
            SceneManager.activeModel.currentTransform.positionZ = parseFloat(positionZTextField);
            SceneManager.activeModel.currentTransform.rotationX = parseFloat(rotationXTextField);
            SceneManager.activeModel.currentTransform.rotationY = parseFloat(rotationYTextField);
            SceneManager.activeModel.currentTransform.rotationZ = parseFloat(rotationZTextField);
            SceneManager.activeModel.currentTransform.scaleX = parseFloat(scaleXTextField);
            SceneManager.activeModel.currentTransform.scaleY = parseFloat(scaleYTextField);
            SceneManager.activeModel.currentTransform.scaleZ = parseFloat(scaleZTextField);
        } else{
            showWarning("Не выбрана конкретная модель");
        }*/

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
    private void shortcutsInfoMenuItemClick(){
        showInfo(ShortcutsSettings.getInfo());
    }

    @FXML
    private void onSaveModelMenuItemClick() {

        try {
            if (SceneManager.activeModel == null) {
                logWarning("Для сохранения надо выбрать конкретную модель");
                showWarning("Выберите конкретную модель");
            } else {
                Optional<SaveVariant> choice = askSaveVariant();
                if (choice.isEmpty()) return;

                Model toSaveModel = null;
                String varik = "";
                SaveVariant variant = choice.get();
                switch (variant) {
                    case ORIGINAL -> {
                        toSaveModel = SceneManager.getOriginalModelFromModifiedModel(SceneManager.activeModel);
                        varik = "Исходная загруженная версия";
                    }
                    case MODIFIED -> {
                        toSaveModel = SceneManager.activeModel;
                        varik = "Измененная версия";
                    }
                }

                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
                fileChooser.setTitle("Save Model");

                File file = fileChooser.showSaveDialog((Stage) sceneCanvas.getScene().getWindow());
                Path fileName = Path.of(file.getAbsolutePath());
                ObjWriter.writeModelToFile(toSaveModel, fileName.toString());
                logInfo(String.format("Модель %s (%s) была успешно сохранена", SceneManager.activeModel.modelName, varik));
            }
        }
        catch(Exception exception){
            logError(exception.getMessage());
            showError(exception.getMessage());
        }
    }

    @FXML
    private void onLoadTextureButtonClick(){
        FileChooser fc = new FileChooser();
            fc.setTitle("Choose texture");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            );

            File texFile = fc.showOpenDialog((Stage) sceneCanvas.getScene().getWindow());
            if (texFile != null) {
                Image tex = new Image(texFile.toURI().toString());
                SceneManager.activeModel.texture = tex;
                SceneManager.activeModel.hasTexture = true;
                SceneManager.activeModel.textureName = texFile.getName();
                currentTextureLabel.setText(String.format("Текущая текстура: %s", SceneManager.activeModel.textureName));
                deleteTextureButton.setVisible(true);
            } else {
                SceneManager.activeModel.texture = null;
                SceneManager.activeModel.hasTexture = false;
            }


    }


    @FXML
    private void onOpenModelMenuItemClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
            fileChooser.setTitle("Load Model");

            File file = fileChooser.showOpenDialog((Stage) sceneCanvas.getScene().getWindow());
            if (file == null) {
                return;
            }

            Path fileName = Path.of(file.getAbsolutePath());


            String fileContent = Files.readString(fileName);
            Model mesh = ObjReader.readModelFromFile(fileContent, fileName.getFileName().toString(), SceneManager.historyModelName);
            validateAndCorrectDuplicateModelName(mesh);

            Model meshClone = mesh.deepCopy();
            mesh.triangulate();

            //Добавление кнопки
            addModelButton(mesh, meshClone);
            logInfo(String.format("Модель %s была успешно загружена", mesh.modelName));
        } catch (Exception exception) {
            logError(exception.getMessage());
            showError(exception.getMessage());
        }
    }

    public void logInfo(String text) {
        appendLog("INFO", text);
    }

    public void logWarning(String text) {
        appendLog("WARN", text);
    }

    public void logError(String text) {
        appendLog("ERROR", text);
    }

    protected static void validateAndCorrectDuplicateModelName(Model targetModel){
        try {
            if (SceneManager.historyModelName.containsKey(targetModel.modelName)) {
                int c = SceneManager.historyModelName.get(targetModel.modelName);
                SceneManager.historyModelName.put(targetModel.modelName, ++c);
                targetModel.modelName += String.format(" (%d)", c);
            } else {
                SceneManager.historyModelName.put(targetModel.modelName, 0);
            }
        }
        catch (Exception exception){
            throw new RuntimeException("Ошибка при проверки и корректировки дубликата имени модельки");
        }
    }

    public void showWarning(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(null);
        alert.setContentText(text);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/main_icon.png"))
        ));

        alert.showAndWait();
    }

    public void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(text);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/main_icon.png"))
        ));

        alert.showAndWait();
    }

    public boolean askConfirm(String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(null);
        alert.setContentText(text);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/main_icon.png"))
        ));

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void showInfo(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(text);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/main_icon.png"))
        ));

        alert.showAndWait();
    }
    @FXML private void onDeleteTextureButtonClick(ActionEvent event){
        SceneManager.activeModel.hasTexture = false;
        SceneManager.activeModel.texture = Model.defaultTexture;
        SceneManager.activeModel.textureName = "По умолчанию";
        currentTextureLabel.setText("Текущая текстура: По умолчанию");
        deleteTextureButton.setVisible(false);

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
    private void renderButtonClick(ActionEvent event){
        renderFrame();
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

        deleteTextureButton.setVisible(false);
        String currentTextureName = "По умолчанию";
        loadTextureButton.setVisible(true);
        if (!model.getHasTextureVertex()){
            currentTextureName = "У модели отсутствуют текстурные координаты";
            loadTextureButton.setVisible(false);
        } else if(model.hasTexture && model.textureName != null && model.texture != null){
            currentTextureName = model.textureName;
            deleteTextureButton.setVisible(true);
        }

        currentTextureLabel.setText(String.format("Текущая текстура: %s", currentTextureName));

        setTextFieldModelTransform(model);

        deleteActiveEntityButton.setVisible(true);
        transformationTitledPane.setVisible(true);
    }

    private void setTextFieldModelTransform(Model model){
        positionXTextField.setText(""+model.currentTransform.positionX);
        positionYTextField.setText(""+model.currentTransform.positionY);
        positionZTextField.setText(""+model.currentTransform.positionZ);
        rotationXTextField.setText(""+model.currentTransform.rotationX);
        rotationYTextField.setText(""+model.currentTransform.rotationY);
        rotationZTextField.setText(""+model.currentTransform.rotationZ);
        scaleXTextField.setText(""+model.currentTransform.scaleX);
        scaleYTextField.setText(""+model.currentTransform.scaleY);
        scaleZTextField.setText(""+model.currentTransform.scaleZ);

    }

    private void addModelButton(Model model, Model clone) {
        SceneManager.loadModelToScene(model);
        SceneManager.loadOriginalModelToScene(clone);

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
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            rotateCamera(mouseEvent);
        } else {
            moveCamera(mouseEvent);
        }
    }

    private void moveCamera(MouseEvent mouseEvent) {
        float deltaX = (float) (mouseEvent.getX() - pastMoveX);
        float deltaY = (float) (mouseEvent.getY() - pastMoveY);
        pastMoveX = (float) mouseEvent.getX();
        pastMoveY = (float) mouseEvent.getY();

        SceneManager.activeCamera.moveCamera(deltaX * ROT, deltaY * ROT);
        if(currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
            renderFrame();
        }
    }

    private void rotateCamera(MouseEvent mouseEvent) {
        float deltaX = (float) (mouseEvent.getX() - pastRotateX);
        float deltaY = (float) (mouseEvent.getY() - pastRotateY);
        pastRotateX = (float) mouseEvent.getX();
        pastRotateY = (float) mouseEvent.getY();

        SceneManager.activeCamera.rotateCamera(-deltaX * ROT, -deltaY * ROT);
        if(currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
            renderFrame();
        }
    }

    private void setPastXY(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            setPastRotateXY(mouseEvent);
        } else {
            setPastMoveXY(mouseEvent);
        }
    }

    private void setPastMoveXY(MouseEvent mouseEvent) {
        pastMoveX = (float) mouseEvent.getX();
        pastMoveY = (float) mouseEvent.getY();
    }

    private void setPastRotateXY(MouseEvent mouseEvent) {
        pastRotateX = (float) mouseEvent.getX();
        pastRotateY = (float) mouseEvent.getY();
    }

    public void setZoom(ScrollEvent scrollEvent) {
        SceneManager.activeCamera.zoomCamera((float) scrollEvent.getDeltaY() / 20);
    }
}