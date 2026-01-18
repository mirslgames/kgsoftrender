package com.cgvsu.sceneview;

import com.cgvsu.model.Vertex;
import com.cgvsu.modelOperations.ZBuffer;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.service.ShortcutsSettings;
import com.cgvsu.service.ThemeSettings;
import javafx.beans.binding.Bindings;
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

import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
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

import static java.nio.file.Path.*;

public class GuiController {

    //private static final Logger log = LoggerFactory.getLogger(GuiController.class);
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

    private final ArrayList<Button> vertexButtons = new ArrayList<>();
    private Button activeVertexButton;

    private final ArrayList<Button> polygonButtons = new ArrayList<>();
    private Button activePolygonButton;

    private final ArrayList<Button> cameraButtons = new ArrayList<>();
    private Button activeCameraButton;

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
    @FXML
    private SplitPane canvasSplitPane;
    @FXML
    private TitledPane camersPane;
    @FXML
    private VBox camersBox;
    @FXML
    private VBox polygonsBox;
    @FXML
    private VBox vertexBox;
    @FXML
    private TitledPane polygonPane;
    @FXML
    private TitledPane vertexPane;
    @FXML
    private Button deleteVertex;
    @FXML
    private Button deletePolygon;
    @FXML
    private CheckBox deleteFreeVertexCheckbox;
    @FXML
    private Button createCameraButton;
    @FXML
    private Button deleteCameraButton;


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
                    Bindings.format(
                            "Интенсивность освещения: %.2f",
                            Bindings.subtract(1.0, lightIntensitySlider.valueProperty())
                    )
            );

            lightIntensitySlider.valueProperty().addListener((obs, oldV, newV) -> {
                SceneManager.lightIntensity = newV.floatValue();
                if(currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                        currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                        currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                    renderFrame();
                }

            });

            deleteVertex.setVisible(false);
            deletePolygon.setVisible(false);
            deleteFreeVertexCheckbox.setVisible(false);
            vertexPane.setVisible(false);
            polygonPane.setVisible(false);

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
            canvasParentAnchorPane.setOnMouseClicked(this::setDefaultPosition);
            canvasParentAnchorPane.setOnScroll(this::setZoom);


            SceneManager.initialize();

            Platform.runLater(() -> {
                ThemeSettings.setLightTheme();
                applyTheme();

                Scene scene = sceneCanvas.getScene();
                if (scene != null) {
                    installHoverForAllButtons(scene.getRoot());
                }

                deleteCameraButton.setVisible(false);
                generateCameraButtons();

            });

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
                renderFrame();
            });

            useTextureCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                SceneManager.useTexture = newVal;
                renderFrame();
            });

            useLightCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                SceneManager.useLight = newVal;
                renderFrame();
            });




            timeline = new Timeline();
            timeline.setCycleCount(Animation.INDEFINITE);

            KeyFrame frame = new KeyFrame(Duration.millis(30), e -> {
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

        ZBuffer zBuffer = new ZBuffer((int) width, (int) height);
        zBuffer.clear();
        for (Model model : SceneManager.models) {
            RenderEngine.renderWithRenderingMods(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, model, (int) width, (int) height, zBuffer);
            //RenderEngine.render(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, model, (int) width, (int) height);
        }
        //ВАРИАНТ рендерить только активную модель
        /*if (SceneManager.acti
        veModel != null) {
            RenderEngine.render(sceneCanvas.getGraphicsContext2D(), SceneManager.activeCamera, SceneManager.activeModel, (int) width, (int) height);
        }*/
    }

    private void generateCameraButtons(){
        try {
            for (int i = 0; i < SceneManager.cameras.size(); i++) {
                String btnName = SceneManager.cameras.get(i).cameraName;
                Button btn = new Button(btnName);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(this::onCameraButtonClick);

                installHoverForCameraButton(btn);
                cameraButtons.add(btn);
                btn.setStyle(ThemeSettings.buttonStyle);
                camersBox.getChildren().add(btn);

                if (SceneManager.activeCamera.cameraName.equals(btnName)) {
                    activeCameraButton = btn;
                    activeCameraButton.setStyle(ThemeSettings.activeButtonStyle);
                }
            }
        } catch(Exception exception){
            logError("Ошибка при генерации кнопок для камер");
            showError("Ошибка при генерации кнопок для камер");
        }
    }

    @FXML
    private void deleteCameraButtonClick(ActionEvent event) {
        String logString = "";
        try {
            Camera currentActiveCamera = SceneManager.activeCamera;
            if (currentActiveCamera.cameraName.equals("Начальная камера")) {
                showWarning("Вы не можете удалить начальную камеру");
            } else {
                logString = currentActiveCamera.cameraName;
                SceneManager.deleteCameraFromScene(currentActiveCamera.cameraName);
                for (Camera camera : SceneManager.cameras) {
                    if (camera.cameraName.equals("Начальная камера")) {
                        SceneManager.activeCamera = camera;
                        break;
                    }
                }
                cameraButtons.clear();
                camersBox.getChildren().clear();
                deleteCameraButton.setVisible(false);
                generateCameraButtons();
                logInfo(String.format("Камера: %s была успешно удалена", logString));
            }


        } catch (Exception exception){
            logError("Ошибка при удалении камеры");
            showError("Ошибка при удалении камеры");
        }

    }

    private void onCameraButtonClick(ActionEvent event){
        try {
            Button button = (Button) event.getSource();
            String cameraName = button.getText();
            Camera targetCamera = SceneManager.cacheNameCameras.get(cameraName);
            SceneManager.activeCamera = targetCamera;
            if (activeCameraButton != null) {
                activeCameraButton.setStyle(ThemeSettings.buttonStyle);
            }
            activeCameraButton = button;
            activeCameraButton.setStyle(ThemeSettings.activeButtonStyle);
            deleteCameraButton.setVisible(true);
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        } catch (Exception exception){
            logError("Ошибка при выборе камеры");
            showError("Ошибка при выборе камеры");
        }
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
    private void createCameraButtonClick(ActionEvent event){
        try{
            SceneManager.createNewCamera();
            cameraButtons.clear();
            camersBox.getChildren().clear();
            generateCameraButtons();
            logInfo("Камера была успешно создана");
        } catch (Exception exception){
            logError("Ошибка при создании новой камеры");
            showError("Ошибка при создании новой камеры");
        }
    }

    @FXML
    private void onLightThemeMenuItemClick() {
        ThemeSettings.setLightTheme();
        applyTheme();
        if(currentRenderMode != RenderMode.EVERY_FRAME){
            renderFrame();
        }
    }

    @FXML
    private void onDarkThemeMenuItemClick() {
        ThemeSettings.setDarkTheme();
        applyTheme();
        if(currentRenderMode != RenderMode.EVERY_FRAME){
            renderFrame();
        }
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
        renderFrame();
        renderButton.setDisable(true);
    }
    @FXML private void cameraFrameMenuItemClick(ActionEvent event){
        currentRenderMode = RenderMode.EVERY_CAMERA_MOTION_FRAME;
        renderFrame();
        renderButton.setDisable(true);
    }
    @FXML private void cameraTransformFrameMenuItemClick(ActionEvent event){
        currentRenderMode = RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME;
        renderFrame();
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

    private void installHoverForVertexButton(ButtonBase button) {
        if (button == null) return;

        button.setOnMouseEntered(e -> {
            if (button == activeVertexButton) {
                button.setStyle(ThemeSettings.activeButtonStyle);
            } else {
                button.setStyle(ThemeSettings.buttonHoverStyle);
            }
        });

        button.setOnMouseExited(e -> {
            if (button == activeVertexButton) {
                button.setStyle(ThemeSettings.activeButtonStyle);
            } else {
                button.setStyle(ThemeSettings.buttonStyle);
            }
        });
    }

    private void installHoverForPolygonButton(ButtonBase button) {
        if (button == null) return;

        button.setOnMouseEntered(e -> {
            if (button == activePolygonButton) {
                button.setStyle(ThemeSettings.activeButtonStyle);
            } else {
                button.setStyle(ThemeSettings.buttonHoverStyle);
            }
        });

        button.setOnMouseExited(e -> {
            if (button == activePolygonButton) {
                button.setStyle(ThemeSettings.activeButtonStyle);
            } else {
                button.setStyle(ThemeSettings.buttonStyle);
            }
        });
    }

    private void installHoverForCameraButton(ButtonBase button) {
        if (button == null) return;

        button.setOnMouseEntered(e -> {
            if (button == activeCameraButton) {
                button.setStyle(ThemeSettings.activeButtonStyle);
            } else {
                button.setStyle(ThemeSettings.buttonHoverStyle);
            }
        });

        button.setOnMouseExited(e -> {
            if (button == activeCameraButton) {
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
        StringBuilder sb = new StringBuilder();
        sb.append(ShortcutsSettings.getInfo());
        sb.append("\n===Управление камерой===\n");
        sb.append("ЛКМ - поворот камеры\n");
        sb.append("ПКМ или Shift + СКМ - перемещение камеры\n");
        sb.append("Колесико мыши - зум\n");
        sb.append("Двойной клик - вернуться обратно\n");
        showInfo(sb.toString());
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
                        toSaveModel = SceneManager.activeModel.copyWithTransform();
                        varik = "Измененная версия";
                    }
                }

                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
                fileChooser.setTitle("Save Model");

                File file = fileChooser.showSaveDialog((Stage) sceneCanvas.getScene().getWindow());
                if (file == null) return;

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
        try {
            if(SceneManager.activeModel != null) {
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
                    logInfo(String.format("Текстура %s была успешно загружена", SceneManager.activeModel.textureName));
                    if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                            currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                            currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                        renderFrame();
                    }

                } else {
                    SceneManager.activeModel.texture = null;
                    SceneManager.activeModel.hasTexture = false;
                }
            } else{
                showError("Не выбрана модель для загрузки текстуры");
            }

        } catch (Exception e) {
            logError("Ошибка при загрузке текстуры");
            showError("Ошибка при загрузке текстуры");;
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


            String fileContent;

            try {
                fileContent = Files.readString(fileName, StandardCharsets.UTF_8);
            } catch (MalformedInputException e) {
                //Если файл не UTF-8 для русской кодировки
                fileContent = Files.readString(fileName, Charset.forName("windows-1251"));
            }

            Model mesh = ObjReader.readModelFromFile(fileContent, fileName.getFileName().toString(), SceneManager.historyModelName);
            validateAndCorrectDuplicateModelName(mesh);

            Model meshClone = mesh.deepCopy();
            mesh.triangulate();

            //Добавление кнопки
            addModelButton(mesh, meshClone);
            renderFrame(); //рендерим кадр сразу после загрузки модели
            logInfo(String.format("Модель %s была успешно загружена", mesh.modelName));

        } catch (ObjReaderException exception) {
            showError(exception.getMessage());
        } catch (Exception exception) {
            logError("Ошибка при загрузке модели" + exception.getMessage());
            showError("Ошибка при загрузке модели" + exception.getMessage());
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
        try {
            String copyName = SceneManager.activeModel.textureName;
            SceneManager.activeModel.hasTexture = false;
            SceneManager.activeModel.texture = Model.defaultTexture;
            SceneManager.activeModel.textureName = "По умолчанию";
            currentTextureLabel.setText("Текущая текстура: По умолчанию");
            deleteTextureButton.setVisible(false);
            logInfo(String.format("Текстура %s была успешно удалена", copyName));
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        } catch (Exception exception){
            logError("Ошибка при удалении текстуры");
            showError("Ошибка при удалении текстуры");
        }

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
            polygonPane.setVisible(false);
            vertexPane.setVisible(false);
            deleteVertex.setVisible(false);
            deletePolygon.setVisible(false);
            deleteFreeVertexCheckbox.setVisible(false);
            activeVertexButton = null;
            activePolygonButton = null;
            vertexButtons.clear();
            polygonButtons.clear();
            vertexBox.getChildren().clear();
            polygonsBox.getChildren().clear();

            if(currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                    currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }

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

        try {
            polygonPane.setVisible(true);
            vertexPane.setVisible(true);
            vertexButtons.clear();
            polygonButtons.clear();
            vertexBox.getChildren().clear();
            polygonsBox.getChildren().clear();

            if (activeVertexButton != null) {
                activeVertexButton.setStyle(ThemeSettings.buttonStyle);
            }
            activeVertexButton = null;

            if (activePolygonButton != null) {
                activePolygonButton.setStyle(ThemeSettings.buttonStyle);
            }
            activePolygonButton = null;

            deleteVertex.setVisible(false);
            deletePolygon.setVisible(false);
            deleteFreeVertexCheckbox.setVisible(false);

            generateVertexButtonsFromModel(model.vertices);
            generatePolygonButtonsFromModel(model.polygonsBoundaries);
        }
        catch(Exception exception){
            logError("Ошибка при извлечении вершин и полигонов из модели");
            showError("Ошибка при извлечении вершин и полигонов из модели");
        }
    }

    private void generateVertexButtonsFromModel(ArrayList<Vertex> vertices){
        for(int i = 0; i < vertices.size(); i++){
            String btnName = String.format("Вершина %d", i);
            Button btn = new Button(btnName);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(this::onVertexButtonClick);

            installHoverForVertexButton(btn);
            vertexButtons.add(btn);
            btn.setStyle(ThemeSettings.buttonStyle);
            vertexBox.getChildren().add(btn);
        }

    }

    private void generatePolygonButtonsFromModel(ArrayList<Integer> polygons){
        for(int i = 0; i < polygons.size(); i++){
            String btnName = String.format("Полигон %d", i);
            Button btn = new Button(btnName);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(this::onPolygonButtonClick);

            installHoverForPolygonButton(btn);
            polygonButtons.add(btn);
            btn.setStyle(ThemeSettings.buttonStyle);
            polygonsBox.getChildren().add(btn);
        }

    }

    private void onPolygonButtonClick(ActionEvent event){
        //В этот момент надо подсвечивать на рендере полигон
        try {
            Button button = (Button) event.getSource();
            String btnText = button.getText();
            if (activePolygonButton != null) {
                activePolygonButton.setStyle(ThemeSettings.buttonStyle);
            }
            activePolygonButton = button;
            activePolygonButton.setStyle(ThemeSettings.activeButtonStyle);
            deleteVertex.setVisible(false);
            deletePolygon.setVisible(true);
            deleteFreeVertexCheckbox.setVisible(true);
            deleteActiveEntityButton.setVisible(false);

            if (activeVertexButton != null) {
                activeVertexButton.setStyle(ThemeSettings.buttonStyle);
            }
            activeVertexButton = null;
        } catch(Exception exception){
            logError("Ошибка при выборе полигона");
            showError("Ошибка при выборе полигона");
        }

    }

    private void onVertexButtonClick(ActionEvent event){
        //В этот момент надо подсвечивать на рендере вершину
        try {
            Button button = (Button) event.getSource();
            String btnText = button.getText();
            if (activeVertexButton != null) {
                activeVertexButton.setStyle(ThemeSettings.buttonStyle);
            }
            activeVertexButton = button;
            activeVertexButton.setStyle(ThemeSettings.activeButtonStyle);
            deleteVertex.setVisible(true);
            deletePolygon.setVisible(false);
            deleteFreeVertexCheckbox.setVisible(false);
            deleteActiveEntityButton.setVisible(false);

            if (activePolygonButton != null) {
                activePolygonButton.setStyle(ThemeSettings.buttonStyle);
            }
            activePolygonButton = null;
        } catch (Exception exception){
            logError("Ошибка при выборе вершины");
            showError("Ошибка при выборе вершины");
        }
    }

    private boolean ensureActiveModel() {
        if (SceneManager.activeModel == null) {
            showError("Сначала выберите модель");
            return false;
        }
        return true;
    }


    @FXML
    private void deleteVertexClick(ActionEvent event){
        //Удаление вершины у модели
        if (!ensureActiveModel()) return;
        try {
            int index = Integer.parseInt(activeVertexButton.getText().replaceAll("\\D+", ""));
            boolean result = SceneManager.activeModel.deleteVertexFromIndex(index);

            if (!result) {
                logError("Ошибка при удалении вершины: " + index);
                showError("Ошибка при удалении вершины: " + index);
                return;
            }

            deleteVertex.setVisible(false);
            if (activeVertexButton != null) {
                activeVertexButton.setStyle(ThemeSettings.buttonStyle);
            }
            activeVertexButton = null;
            vertexButtons.clear();
            vertexBox.getChildren().clear();
            generateVertexButtonsFromModel(SceneManager.activeModel.vertices);

            deletePolygon.setVisible(false);
            deleteFreeVertexCheckbox.setVisible(false);
            if (activePolygonButton != null) {
                activePolygonButton.setStyle(ThemeSettings.buttonStyle);
            }
            activePolygonButton = null;
            polygonButtons.clear();
            polygonsBox.getChildren().clear();
            generatePolygonButtonsFromModel(SceneManager.activeModel.polygonsBoundaries);

            logInfo(String.format("Вершина %d была успешно удалена", index));
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }

        } catch(Exception exception){
            logError("Ошибка при удалении вершин");
            showError("Ошибка при удалении вершин");
        }
    }

    @FXML
    private void deletePolygonClick(ActionEvent event){
        //Удаление полигона у модели
        if (!ensureActiveModel()) return;

        try {
            boolean removeFreeVertex = deleteFreeVertexCheckbox.isSelected();
            int index = Integer.parseInt(activePolygonButton.getText().replaceAll("\\D+", ""));
            boolean result = SceneManager.activeModel.deletePolygonFromIndex(index, removeFreeVertex);

            if (!result) {
                logError("Ошибка при удалении полигона: " + index);
                showError("Ошибка при удалении полигона: " + index);
                return;
            }

            deletePolygon.setVisible(false);
            deleteFreeVertexCheckbox.setVisible(false);
            if (activePolygonButton != null) {
                activePolygonButton.setStyle(ThemeSettings.buttonStyle);
            }
            activePolygonButton = null;
            polygonButtons.clear();
            polygonsBox.getChildren().clear();
            generatePolygonButtonsFromModel(SceneManager.activeModel.polygonsBoundaries);

            deleteVertex.setVisible(false);
            if (activeVertexButton != null) {
                activeVertexButton.setStyle(ThemeSettings.buttonStyle);
            }
            activeVertexButton = null;
            vertexButtons.clear();
            vertexBox.getChildren().clear();
            generateVertexButtonsFromModel(SceneManager.activeModel.vertices);

            logInfo(String.format("Полигон %d был успешно удален", index));
            if(currentRenderMode == RenderMode.EVERY_TRANSFORM_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }

        } catch (Exception exception){
            logError("Ошибка при удалении полигона");
            showError("Ошибка при удалении полигона");
        }
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
        installHoverForButton(btn);

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

        SceneManager.activeCamera.moveCamera(-deltaX, deltaY,
                (int) sceneCanvas.getWidth(), (int) sceneCanvas.getHeight());
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
        SceneManager.activeCamera.zoomCamera((float) scrollEvent.getDeltaY() / 10);
        if(currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
            renderFrame();
        }
    }

    public void setDefaultPosition(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            SceneManager.activeCamera.returnToDefaultCamera();
            if(currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_FRAME ||
                    currentRenderMode == RenderMode.EVERY_CAMERA_MOTION_TRANSFORM_FRAME){
                renderFrame();
            }
        }
    }
}