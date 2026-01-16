package com.cgvsu.modelOperations;

import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.sceneview.SceneManager;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Класс для наложения текстуры на полигоны с учётом Z-буфера.
 * Обеспечивает правильное отображение текстур, учитывая глубину для предотвращения
 * отрисовки задних полигонов поверх передних.
 */
public class TextureMapping {

    /**
     * Получает цвет пикселя из текстуры по текстурным координатам.
     *
     * @param texture текстура (Image из JavaFX)
     * @param texCoord текстурные координаты (u, v) в диапазоне [0, 1]
     * @return цвет пикселя из текстуры, или белый цвет если текстура null
     */
    public static Color getTextureColor(Image texture, Vector2f texCoord) {
        if (texture == null || texCoord == null) {
            return Color.WHITE;
        }

        PixelReader pixelReader = texture.getPixelReader();
        if (pixelReader == null) {
            return Color.WHITE;
        }

        // Преобразуем нормализованные координаты [0, 1] в координаты текстуры
        float u = texCoord.getX();
        float v = texCoord.getY();

        // Обрабатываем повторение текстуры (wrap mode)
        // Если координаты выходят за [0, 1], повторяем текстуру
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        v = 1.0f - v;
        if (v >= 1.0f) v = 0.0f;

        // Преобразуем в координаты пикселя в текстуре
        int texX = (int) (u * (texture.getWidth() - 1));
        int texY = (int) (v * (texture.getHeight() - 1));

        // Ограничиваем координаты размерами текстуры
        texX = Math.max(0, Math.min(texX, (int) texture.getWidth() - 1));
        texY = Math.max(0, Math.min(texY, (int) texture.getHeight() - 1));

        // Читаем цвет пикселя
        return pixelReader.getColor(texX, texY);
    }

    /**
     * Получает цвет пикселя из текстуры с учётом Z-буфера.
     * Пиксель будет отрисован только если он проходит проверку Z-буфера.
     *
     * @param texture текстура
     * @param texCoord текстурные координаты
     * @param x, y координаты пикселя на экране
     * @param z глубина пикселя
     * @param zBuffer Z-буфер для проверки глубины
     * @return цвет пикселя из текстуры, или null если пиксель не должен быть отрисован (закрыт другим)
     */
    public static Color getTextureColorWithZBuffer(
            Image texture,
            Vector2f texCoord,
            int x, int y,
            float z,
            ZBuffer zBuffer) {

        // Проверяем Z-буфер
        if (zBuffer == null || !zBuffer.testAndSet(x, y, z)) {
            return null; // Пиксель закрыт другим, не рисуем
        }

        // Получаем цвет из текстуры
        return getTextureColor(texture, texCoord);
    }

    /**
     *
     * @param ray луч света от камеры
     * @param normal нормаль в текущей точке
     * @param baseColor базовые цвет из текстуры
     * @param lightIntensity интенсивность света, задаётся через интерфейс
     * @return цвет с учётом освещения
     */
    public static Color getModifiedColorWithLighting(Vector3f ray, Vector3f normal, Color baseColor,
                                                     float lightIntensity){

        float l = -1 * ray.dot(normal);
        if (l < 0){
            l = 0;
        }
        double new_r = baseColor.getRed() * (1-lightIntensity) + baseColor.getRed() * l * lightIntensity;
        double new_g = baseColor.getGreen() * (1-lightIntensity) + baseColor.getGreen() * l * lightIntensity;
        double new_b = baseColor.getBlue() * (1-lightIntensity) + baseColor.getBlue() * l * lightIntensity;
        return new Color(new_r, new_g, new_b, baseColor.getOpacity());
    }

}


