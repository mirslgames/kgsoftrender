package com.cgvsu.sceneview;

import com.cgvsu.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SceneManagerTest {

    // Сбрасываем сцену для каждого теста, потому что SceneManager статический
    @BeforeEach
    void resetScene() {
        SceneManager.models.clear();
        SceneManager.cacheNameSceneModels = new java.util.Hashtable<>();
        SceneManager.activeModel = null;
        SceneManager.isSceneEntitySelect = false;
        SceneManager.activeCamera = null;
    }

    @Test
    void testInitializeSetsCameraAndFlags() {
        SceneManager.initialize();

        assertNotNull(SceneManager.activeCamera,
                "initialize(): activeCamera должна быть создана (не null).");

        assertFalse(SceneManager.isSceneEntitySelect,
                "initialize(): isSceneEntitySelect должен быть false после инициализации.");
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
    void testRemoveExistingRemovesFromListAndCache() {
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

        assertFalse(SceneManager.models.contains(m),
                "removeModelFromScene(String): после повторного удаления модель всё равно не должна появляться в models.");

        assertNull(SceneManager.cacheNameSceneModels.get("m1"),
                "removeModelFromScene(String): после повторного удаления модель всё равно не должна появляться в cacheNameSceneModels.");
    }
}
