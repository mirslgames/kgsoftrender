package com.cgvsu.sceneview;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.*;

public class SceneManagerTest {

    // Сбрасываем сцену для каждого теста, потому что SceneManager статический
    @BeforeEach
    void resetScene() {
        SceneManager.models.clear();
        SceneManager.cacheNameSceneModels = new Hashtable<>();
        SceneManager.activeModel = null;

        SceneManager.originalModels.clear();
        SceneManager.originalCacheNameSceneModels = new Hashtable<>();

        SceneManager.cameras.clear();
        SceneManager.cacheNameCameras = new Hashtable<>();
        SceneManager.activeCamera = null;

        SceneManager.isSceneEntitySelect = false;
    }


    @Test
    void testInitializeSetsCameraAndFlagsAndCaches() {
        SceneManager.initialize();

        assertNotNull(SceneManager.activeCamera,
                "initialize(): activeCamera должна быть создана (не null).");

        assertEquals("Начальная камера", SceneManager.activeCamera.cameraName,
                "initialize(): имя начальной камеры должно быть 'Начальная камера'.");

        assertFalse(SceneManager.isSceneEntitySelect,
                "initialize(): isSceneEntitySelect должен быть false после инициализации.");

        assertEquals(1, SceneManager.cameras.size(),
                "initialize(): после инициализации должна быть ровно 1 камера в списке.");

        assertSame(SceneManager.activeCamera, SceneManager.cameras.get(0),
                "initialize(): activeCamera должна лежать в cameras[0].");

        assertSame(SceneManager.activeCamera, SceneManager.cacheNameCameras.get("Начальная камера"),
                "initialize(): activeCamera должна лежать в cacheNameCameras по ключу её имени.");
    }


    @Test
    void testCreateNewCameraAddsToListAndCache() {
        SceneManager.initialize();
        int before = SceneManager.cameras.size();

        SceneManager.createNewCamera();

        assertEquals(before + 1, SceneManager.cameras.size(),
                "createNewCamera(): должна добавлять камеру в список cameras.");

        Camera created = SceneManager.cameras.get(SceneManager.cameras.size() - 1);

        assertNotNull(created.cameraName, "createNewCamera(): cameraName не должен быть null.");
        assertTrue(created.cameraName.startsWith("Камера "),
                "createNewCamera(): имя камеры должно начинаться с 'Камера '.");

        assertSame(created, SceneManager.cacheNameCameras.get(created.cameraName),
                "createNewCamera(): камера должна появиться в cacheNameCameras по своему имени.");
    }

    @Test
    void testDeleteCameraFromSceneRemovesFromListAndCache() {
        SceneManager.initialize();
        SceneManager.createNewCamera();

        Camera created = SceneManager.cameras.get(SceneManager.cameras.size() - 1);
        String name = created.cameraName;

        assertNotNull(SceneManager.cacheNameCameras.get(name),
                "Перед удалением: камера должна быть в кеше.");

        int before = SceneManager.cameras.size();

        SceneManager.deleteCameraFromScene(name);

        assertEquals(before - 1, SceneManager.cameras.size(),
                "deleteCameraFromScene(): должна удалять камеру из списка cameras.");

        assertNull(SceneManager.cacheNameCameras.get(name),
                "deleteCameraFromScene(): должна удалять камеру из cacheNameCameras.");

        assertNotNull(SceneManager.cacheNameCameras.get("Начальная камера"),
                "deleteCameraFromScene(): начальная камера не должна удаляться, если удаляли другую.");
    }

    @Test
    void testDeleteCameraFromSceneNonExistingDoesNotCrash() {
        SceneManager.initialize();
        int before = SceneManager.cameras.size();

        assertDoesNotThrow(() -> SceneManager.deleteCameraFromScene("no_such_camera"),
                "deleteCameraFromScene(): удаление несуществующей камеры не должно падать.");

        assertEquals(before, SceneManager.cameras.size(),
                "deleteCameraFromScene(): при удалении несуществующей камеры список cameras не должен меняться.");
    }


    @Test
    void testLoadModelToSceneAddsToListAndCache() {
        Model m = new Model();
        m.modelName = "m1";

        SceneManager.loadModelToScene(m);

        assertTrue(SceneManager.models.contains(m),
                "loadModelToScene(): модель должна появиться в списке models.");

        assertSame(m, SceneManager.cacheNameSceneModels.get("m1"),
                "loadModelToScene(): модель должна быть доступна в cacheNameSceneModels по ключу modelName.");
    }

    @Test
    void testLoadOriginalModelToSceneAddsToOriginalListAndCache() {
        Model original = new Model();
        original.modelName = "m1";

        SceneManager.loadOriginalModelToScene(original);

        assertTrue(SceneManager.originalModels.contains(original),
                "loadOriginalModelToScene(): модель должна появиться в списке originalModels.");

        assertSame(original, SceneManager.originalCacheNameSceneModels.get("m1"),
                "loadOriginalModelToScene(): модель должна быть доступна в originalCacheNameSceneModels по ключу modelName.");
    }

    @Test
    void testRemoveNullReturnsFalse() {
        assertFalse(SceneManager.removeModelFromScene((Model) null),
                "removeModelFromScene(Model): при null должен возвращать false.");

        assertFalse(SceneManager.removeModelFromScene((String) null),
                "removeModelFromScene(String): при null должен возвращать false.");
    }

    @Test
    void testRemoveNonExistingReturnsFalse() {
        SceneManager.initialize();
        SceneManager.activeModel = new Model();
        SceneManager.activeModel.modelName = "active";

        assertFalse(SceneManager.removeModelFromScene("nope"),
                "removeModelFromScene(String): для несуществующей модели должен возвращать false.");

        assertEquals(0, SceneManager.models.size(),
                "removeModelFromScene(String): при удалении несуществующей модели список models не должен меняться.");

        assertNull(SceneManager.cacheNameSceneModels.get("nope"),
                "removeModelFromScene(String): cacheNameSceneModels не должен содержать ключ несуществующей модели.");

        assertNotNull(SceneManager.activeModel,
                "removeModelFromScene(String): удаление несуществующей модели не должно сбрасывать activeModel.");
    }

    @Test
    void testRemoveExistingRemovesFromModelsAndCache() {
        SceneManager.initialize();

        Model m = new Model();
        m.modelName = "m1";
        SceneManager.loadModelToScene(m);

        assertTrue(SceneManager.removeModelFromScene("m1"),
                "removeModelFromScene(String): удаление существующей модели должно возвращать true.");

        assertFalse(SceneManager.models.contains(m),
                "removeModelFromScene(String): удалённой модели не должно быть в списке models.");

        assertNull(SceneManager.cacheNameSceneModels.get("m1"),
                "removeModelFromScene(String): удалённой модели не должно быть в cacheNameSceneModels.");
    }

    @Test
    void testRemoveAlsoRemovesFromOriginalCollections() {
        SceneManager.initialize();

        Model modified = new Model();
        modified.modelName = "m1";
        SceneManager.loadModelToScene(modified);

        Model original = new Model();
        original.modelName = "m1";
        SceneManager.loadOriginalModelToScene(original);

        assertTrue(SceneManager.removeModelFromScene("m1"),
                "removeModelFromScene(String): должно удалять и модифицированную, и оригинальную модель.");

        assertNull(SceneManager.cacheNameSceneModels.get("m1"),
                "removeModelFromScene(String): должно удалять из cacheNameSceneModels.");

        assertNull(SceneManager.originalCacheNameSceneModels.get("m1"),
                "removeModelFromScene(String): должно удалять из originalCacheNameSceneModels.");

        assertTrue(SceneManager.models.isEmpty(),
                "removeModelFromScene(String): список models должен стать пустым.");

        assertTrue(SceneManager.originalModels.isEmpty(),
                "removeModelFromScene(String): список originalModels должен стать пустым.");
    }

    @Test
    void testRemoveActiveModelResetsActiveAndSelection() {
        SceneManager.initialize();

        Model m = new Model();
        m.modelName = "m1";
        SceneManager.loadModelToScene(m);

        SceneManager.activeModel = m;
        SceneManager.isSceneEntitySelect = true;

        assertTrue(SceneManager.removeModelFromScene("m1"),
                "removeModelFromScene(String): удаление активной модели должно возвращать true.");

        assertNull(SceneManager.activeModel,
                "removeModelFromScene(String): если удалена активная модель, activeModel должен стать null.");

        assertFalse(SceneManager.isSceneEntitySelect,
                "removeModelFromScene(String): если удалена активная модель, isSceneEntitySelect должен стать false.");
    }

    @Test
    void testRemoveNotActiveDoesNotTouchActive() {
        SceneManager.initialize();

        Model active = new Model();
        active.modelName = "active";
        SceneManager.loadModelToScene(active);
        SceneManager.activeModel = active;

        Model other = new Model();
        other.modelName = "other";
        SceneManager.loadModelToScene(other);

        assertTrue(SceneManager.removeModelFromScene("other"),
                "removeModelFromScene(String): удаление существующей неактивной модели должно возвращать true.");

        assertSame(active, SceneManager.activeModel,
                "removeModelFromScene(String): удаление неактивной модели не должно менять activeModel.");
    }

    @Test
    void testRemoveByObjectRemovesFromListAndCache() {
        SceneManager.initialize();

        Model m = new Model();
        m.modelName = "m1";
        SceneManager.loadModelToScene(m);

        assertTrue(SceneManager.removeModelFromScene(m),
                "removeModelFromScene(Model): удаление существующей модели по объекту должно возвращать true.");

        assertFalse(SceneManager.models.contains(m),
                "removeModelFromScene(Model): удалённой модели не должно быть в списке models.");

        assertNull(SceneManager.cacheNameSceneModels.get("m1"),
                "removeModelFromScene(Model): удалённой модели не должно быть в cacheNameSceneModels.");
    }

    @Test
    void testRemoveTwiceSecondTimeReturnsFalse() {
        SceneManager.initialize();

        Model m = new Model();
        m.modelName = "m1";
        SceneManager.loadModelToScene(m);

        assertTrue(SceneManager.removeModelFromScene("m1"),
                "removeModelFromScene(String): первое удаление существующей модели должно вернуть true.");

        assertFalse(SceneManager.removeModelFromScene("m1"),
                "removeModelFromScene(String): повторное удаление уже удалённой модели должно вернуть false.");
    }

    @Test
    void testGetOriginalModelFromModifiedModelReturnsOriginal() {
        Model original = new Model();
        original.modelName = "m1";
        SceneManager.loadOriginalModelToScene(original);

        Model modified = new Model();
        modified.modelName = "m1";

        Model result = SceneManager.getOriginalModelFromModifiedModel(modified);

        assertSame(original, result,
                "getOriginalModelFromModifiedModel(): должен вернуть оригинальную модель по имени modelName.");
    }
}
