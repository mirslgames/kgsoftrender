package com.cgvsu.modelOperations;

import com.cgvsu.math.matrixs.Matrix3f;
import com.cgvsu.math.matrixs.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vectors.Vector2f;
import com.cgvsu.math.vectors.Vector3f;
import com.cgvsu.model.Vertex;

/**
 * Класс для растеризации треугольников с использованием барицентрических координат.
 * Предоставляет методы для вычисления барицентрических координат и интерполяции значений.
 * В последствии сюда можно будем засунуть растеризацию прямых для отрисовыванаия сетки
 */
public class Rasterization {

    /**
     * Вычисляет барицентрические координаты точки относительно треугольника.
     *
     * @param point точка для которой вычисляются координаты (в экранных координатах)
     * @param v1, v2, v3 вершины треугольника в экранных координатах (Point2f)
     * @return массив [alpha, beta, gamma] - барицентрические координаты для конкретного треугольника
     */
    public static float[] calculateBarycentricCoordinates(Point2f point, Point2f v1, Point2f v2, Point2f v3) {
        float x = point.getX();
        float y = point.getY();

        float x1 = v1.getX(), y1 = v1.getY();
        float x2 = v2.getX(), y2 = v2.getY();
        float x3 = v3.getX(), y3 = v3.getY();

        // Вычисляем барицентрические координаты через площади
        // Используем решение системы 3 на 3 с 3 уравнениями
        // x = alpha*x1 + beta*x2 + gamma*x3
        // y = alpha*y1 + beta*y2 + gamma*y3
        // 1 = alpha + beta  + gamma
        float denom = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);

        if (Math.abs(denom) < 1e-10f) {
            //Вырожденные случай
            return new float[]{0, 0, 0};
        }

        float alpha = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / denom;
        float beta = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / denom;
        float gamma = 1.0f - alpha - beta;

        return new float[]{alpha, beta, gamma};
    }

    /**
     * Проверяет, находится ли точка внутри треугольника используя барицентрические координаты.
     *
     * @param barycentric барицентрические координаты [alpha, beta, gamma]
     * @return true если точка внутри треугольника
     */
    public static boolean isInsideTriangle(float[] barycentric) {
        if (barycentric == null || barycentric.length < 3) {
            return false;
        }

        float alpha = barycentric[0];
        float beta = barycentric[1];
        float gamma = barycentric[2];

        // Точка внутри если все координаты >= 0 и их сумма ≈ 1
        float sum = alpha + beta + gamma;
        return alpha >= -1e-5f && beta >= -1e-5f && gamma >= -1e-5f &&
                Math.abs(sum - 1.0f) < 1e-5f;
    }

    /**
     * Интерполирует скалярное значение по барицентрическим координатам.
     *
     * @param v1, v2, v3 значения в вершинах треугольника
     * @param barycentric барицентрические координаты [alpha, beta, gamma]
     * @return интерполированное значение
     */
    public static float interpolate(float v1, float v2, float v3, float[] barycentric) {
        if (barycentric == null || barycentric.length < 3) {
            return 0.0f;
        }

        float alpha = barycentric[0];
        float beta = barycentric[1];
        float gamma = barycentric[2];

        return alpha * v1 + beta * v2 + gamma * v3;
    }

    /**
     * Интерполирует вектор Vector3f по барицентрическим координатам.
     * Используется для интерполяции векторов, конкретно в нашем проекте для нормалей.
     *
     * @param v1, v2, v3 векторы в вершинах треугольника
     * @param barycentric барицентрические координаты [alpha, beta, gamma]
     * @return интерполированный вектор
     */
    public static Vector3f interpolate(Vector3f v1, Vector3f v2, Vector3f v3, float[] barycentric) {
        if (barycentric == null || barycentric.length < 3 || v1 == null || v2 == null || v3 == null) {
            return new Vector3f(0, 0, 0);
        }

        float alpha = barycentric[0];
        float beta = barycentric[1];
        float gamma = barycentric[2];

        // Интерполируем каждую компоненту
        float x = alpha * v1.getX() + beta * v2.getX() + gamma * v3.getX();
        float y = alpha * v1.getY() + beta * v2.getY() + gamma * v3.getY();
        float z = alpha * v1.getZ() + beta * v2.getZ() + gamma * v3.getZ();

        return new Vector3f(x, y, z);
    }

    /**
     * Интерполирует вектор Vector2f по барицентрическим координатам.
     * Используется для интерполяции текстурных координат.
     *
     * @param t1, t2, t3 векторы в вершинах треугольника
     * @param barycentric барицентрические координаты [alpha, beta, gamma]
     * @return интерполированный вектор
     */
    public static Vector2f interpolate(Vector2f t1, Vector2f t2, Vector2f t3, float[] barycentric) {
        if (barycentric == null || barycentric.length < 3 || t1 == null || t2 == null || t3 == null) {
            return new Vector2f(0, 0);
        }

        float alpha = barycentric[0];
        float beta = barycentric[1];
        float gamma = barycentric[2];

        float u = alpha * t1.getX() + beta * t2.getX() + gamma * t3.getX();
        float v = alpha * t1.getY() + beta * t2.getY() + gamma * t3.getY();

        return new Vector2f(u, v);
    }

    /**
     * Интерполирует нормаль вершины по барицентрическим координатам.
     * Нормализует результат для получения корректной нормали.
     *
     * @param v1, v2, v3 вершины треугольника
     * @param barycentric барицентрические координаты [alpha, beta, gamma]
     * @return интерполированная и нормализованная нормаль
     */
    public static Vector3f interpolateNormal(Vertex v1, Vertex v2, Vertex v3, float[] barycentric) {
        if (v1 == null || v2 == null || v3 == null ||
                v1.normal == null || v2.normal == null || v3.normal == null) {
            return new Vector3f(0, 1, 0); // Дефолтная нормаль
        }

        Vector3f interpolated = interpolate(v1.normal, v2.normal, v3.normal, barycentric);
        return interpolated.normalized();
    }

    /**
     *
     * @param t1 текстурная координата
     * @param z1 глубина для конкретной вершины
     * @param barycentric барицентрики для полигона
     * @return интерполированную врешину, которая учитывает перспективу в 3d пространстве
     */
    public static Vector2f interpolateWithPerspective(
            Vector2f t1, Vector2f t2, Vector2f t3,
            float z1, float z2, float z3,
            float[] barycentric) {

        if (barycentric == null || barycentric.length < 3 ||
                t1 == null || t2 == null || t3 == null) {
            return new Vector2f(0, 0);
        }

        float alpha = barycentric[0];
        float beta = barycentric[1];
        float gamma = barycentric[2];

        // Вычисляем 1/z для каждой вершины
        float one_over_z1 = 1.0f / z1;
        float one_over_z2 = 1.0f / z2;
        float one_over_z3 = 1.0f / z3;

        // Вычисляем u/z и v/z для каждой вершины
        float u_over_z1 = t1.getX() * one_over_z1;
        float v_over_z1 = t1.getY() * one_over_z1;

        float u_over_z2 = t2.getX() * one_over_z2;
        float v_over_z2 = t2.getY() * one_over_z2;

        float u_over_z3 = t3.getX() * one_over_z3;
        float v_over_z3 = t3.getY() * one_over_z3;

        // Линейная интерполяция в экранном пространстве
        float u_over_z = alpha * u_over_z1 + beta * u_over_z2 + gamma * u_over_z3;
        float v_over_z = alpha * v_over_z1 + beta * v_over_z2 + gamma * v_over_z3;
        float one_over_z = alpha * one_over_z1 + beta * one_over_z2 + gamma * one_over_z3;

        // Восстановление правильных координат
        float u = u_over_z / one_over_z;
        float v = v_over_z / one_over_z;

        return new Vector2f(u, v);
    }

    /**
     * Метод интерполирует нормаль текущей вершины с учётом глубины, корректно отображает её на 3d пространство
     * @param v1 вершина полигона
     * @param z1 глубина этой вершины
     * @param barycentric барицентрики для вершин полигона
     * @return нормаль в конкретной точке по барицентрикам
     */
    public static Vector3f interpolateNormalWithPerspective(
            Vertex v1, Vertex v2, Vertex v3,
            float z1, float z2, float z3,
            float[] barycentric) {

        if (barycentric == null || barycentric.length < 3 ||
                v1 == null || v2 == null || v3 == null ||
                v1.normal == null || v2.normal == null || v3.normal == null) {
            return new Vector3f(0, 1, 0); // нормаль по умолчанию
        }

        float alpha = barycentric[0];
        float beta = barycentric[1];
        float gamma = barycentric[2];

        // 1/z для каждой вершины
        float one_over_z1 = 1.0f / z1;
        float one_over_z2 = 1.0f / z2;
        float one_over_z3 = 1.0f / z3;

        // Вычисляем normal/z для каждой вершины
        float nx_over_z1 = v1.normal.getX() * one_over_z1;
        float ny_over_z1 = v1.normal.getY() * one_over_z1;
        float nz_over_z1 = v1.normal.getZ() * one_over_z1;

        float nx_over_z2 = v2.normal.getX() * one_over_z2;
        float ny_over_z2 = v2.normal.getY() * one_over_z2;
        float nz_over_z2 = v2.normal.getZ() * one_over_z2;

        float nx_over_z3 = v3.normal.getX() * one_over_z3;
        float ny_over_z3 = v3.normal.getY() * one_over_z3;
        float nz_over_z3 = v3.normal.getZ() * one_over_z3;

        // Интерполяция
        float nx_over_z = alpha * nx_over_z1 + beta * nx_over_z2 + gamma * nx_over_z3;
        float ny_over_z = alpha * ny_over_z1 + beta * ny_over_z2 + gamma * ny_over_z3;
        float nz_over_z = alpha * nz_over_z1 + beta * nz_over_z2 + gamma * nz_over_z3;
        float one_over_z = alpha * one_over_z1 + beta * one_over_z2 + gamma * one_over_z3;

        // Восстановление
        float nx = nx_over_z / one_over_z;
        float ny = ny_over_z / one_over_z;
        float nz = nz_over_z / one_over_z;

        return new Vector3f(nx, ny, nz).normalize();
    }
    public static Vector3f interpolateWorldPositionWithPerspective(
            Vector3f worldPos1, Vector3f worldPos2, Vector3f worldPos3,
            float z1, float z2, float z3,
            float[] barycentric) {

        // Тот же код, но работает с мировыми координатами
        float alpha = barycentric[0];
        float beta  = barycentric[1];
        float gamma = barycentric[2];

        float iz1 = 1.0f / z1;
        float iz2 = 1.0f / z2;
        float iz3 = 1.0f / z3;

        Vector3f p1_oz = worldPos1.multiply(iz1);
        Vector3f p2_oz = worldPos2.multiply(iz2);
        Vector3f p3_oz = worldPos3.multiply(iz3);

        Vector3f p_oz = p1_oz.multiply(alpha)
                .add(p2_oz.multiply(beta))
                .add(p3_oz.multiply(gamma));

        float iz = alpha * iz1 + beta * iz2 + gamma * iz3;

        return p_oz.multiply(1.0f / iz);
    }

    /**
     *  Метод интерполирует координаты всех вершин с учётом перспективы, чтобы получить мировую позицию текущей вершины в отрисовке
     * @param p1 координаты первой вершины треугольнгика
     * @param p2 координаты второй вершины треугольнгика
     * @param p3 координаты третьей вершины треугольнгика
     * @param z1 глубина первой вершины треугольнгика
     * @param z2 глубина второй вершины треугольнгика
     * @param z3 глубина третьей вершины треугольнгика
     * @param barycentric барицентрические координаты для тройки вершин
     * @return интерполированную вершину
     */
    public static Vector3f interpolatePositionWithPerspective(
            Vector3f p1, Vector3f p2, Vector3f p3,
            float z1, float z2, float z3,
            float[] barycentric) {

        float alpha = barycentric[0];
        float beta  = barycentric[1];
        float gamma = barycentric[2];

        float iz1 = 1.0f / z1;
        float iz2 = 1.0f / z2;
        float iz3 = 1.0f / z3;

        Vector3f p1_oz = p1.multiply(iz1);
        Vector3f p2_oz = p2.multiply(iz2);
        Vector3f p3_oz = p3.multiply(iz3);

        Vector3f p_oz = p1_oz.multiply(alpha)
                .add(p2_oz.multiply(beta))
                .add(p3_oz.multiply(gamma));

        float iz = alpha * iz1 + beta * iz2 + gamma * iz3;

        return p_oz.multiply(1.0f / iz);
    }

    /**
     * Находит ограничивающий прямоугольник (bounding box) для треугольника.
     *
     * @param v1, v2, v3 вершины треугольника в экранных координатах
     * @return массив [minX, minY, maxX, maxY]
     */
    public static int[] getBoundingBox(Point2f v1, Point2f v2, Point2f v3) {
        int minX = (int) Math.floor(Math.min(Math.min(v1.getX(), v2.getX()), v3.getX()));
        int maxX = (int) Math.ceil(Math.max(Math.max(v1.getX(), v2.getX()), v3.getX()));
        int minY = (int) Math.floor(Math.min(Math.min(v1.getY(), v2.getY()), v3.getY()));
        int maxY = (int) Math.ceil(Math.max(Math.max(v1.getY(), v2.getY()), v3.getY()));

        return new int[]{minX, minY, maxX, maxY};
    }

    /**
     * Растеризует треугольник, вызывая callback для каждого пикселя внутри треугольника.
     *
     * @param v1, v2, v3 вершины треугольника в экранных координатах (Point2f)
     * @param z1, z2, z3 глубины вершин (для Z-буфера)
     * @param vertex1 оригинальные вершины модели (для интерполяции нормалей и текстур)
     * @param pixelCallback callback функция, вызываемая для каждого пикселя
     */
    public static void rasterizeTriangle(
            Point2f v1, Point2f v2, Point2f v3,
            float z1, float z2, float z3,
            Vertex vertex1, Vertex vertex2, Vertex vertex3,
            PixelCallback pixelCallback) {

        // Находим границу полигона(треугольника)
        int[] bbox = getBoundingBox(v1, v2, v3);
        int minX = bbox[0];
        int minY = bbox[1];
        int maxX = bbox[2];
        int maxY = bbox[3];

        // Проходим по всем пикселям в ограничивающем прямоугольнике
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f pixel = new Point2f(x, y);

                // Вычисляем барицентрические координаты
                float[] barycentric = calculateBarycentricCoordinates(pixel, v1, v2, v3);

                // Проверяем, находится ли пиксель внутри треугольника
                if (isInsideTriangle(barycentric)) {
                    float z = interpolate(z1, z2, z3, barycentric);

                    // Интерполируем текстурные координаты
                    Vector2f texCoord = null;
                    if (vertex1 != null && vertex2 != null && vertex3 != null &&
                            vertex1.textureCoordinate != null &&
                            vertex2.textureCoordinate != null &&
                            vertex3.textureCoordinate != null) {
                        texCoord = interpolateWithPerspective(
                                vertex1.textureCoordinate,
                                vertex2.textureCoordinate,
                                vertex3.textureCoordinate,
                                z1, z2, z3,
                                barycentric
                        );
                    }

                    // Интерполируем нормаль
                    Vector3f normal = interpolateNormalWithPerspective(vertex1, vertex2,
                            vertex3, z1, z2, z3, barycentric);
                    Vector3f worldPosition = interpolatePositionWithPerspective(vertex1.position,
                            vertex2.position, vertex3.position,
                            z1, z2, z3, barycentric);
                    // Вызываем callback
                    // Используется для отрисовки
                    pixelCallback.onPixel(x, y, z, barycentric, texCoord, normal, worldPosition);
                }
            }
        }
    }
    public static void rasterizeTriangleWithWorldPos(
            Point2f v1, Point2f v2, Point2f v3,
            float z1, float z2, float z3,
            Vertex vertex1, Vertex vertex2, Vertex vertex3,
            Vector3f worldPos1, Vector3f worldPos2, Vector3f worldPos3,
            PixelCallback pixelCallback, Matrix4f modelMatrix) {

        int[] bbox = getBoundingBox(v1, v2, v3);
        int minX = bbox[0];
        int minY = bbox[1];
        int maxX = bbox[2];
        int maxY = bbox[3];

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Point2f pixel = new Point2f(x, y);
                float[] barycentric = calculateBarycentricCoordinates(pixel, v1, v2, v3);

                if (isInsideTriangle(barycentric)) {
                    float z = interpolate(z1, z2, z3, barycentric);

                    // Интерполируем мировую позицию
                    Vector3f worldPosition = interpolateWorldPositionWithPerspective(
                            worldPos1, worldPos2, worldPos3,
                            z1, z2, z3,
                            barycentric
                    );

                    // Интерполируем текстурные координаты
                    Vector2f texCoord = null;
                    if (vertex1.textureCoordinate != null &&
                            vertex2.textureCoordinate != null &&
                            vertex3.textureCoordinate != null) {
                        texCoord = interpolateWithPerspective(
                                vertex1.textureCoordinate,
                                vertex2.textureCoordinate,
                                vertex3.textureCoordinate,
                                z1, z2, z3,
                                barycentric
                        );
                    }

                    // Интерполируем нормаль
                    Vector3f normal = interpolateNormalWithPerspective(
                            vertex1, vertex2, vertex3,
                            z1, z2, z3,
                            barycentric
                    );
                    Vector3f worldNormal = modelMatrix.multiplyOnVector(normal);
                    worldNormal.normalize();

                    pixelCallback.onPixel(x, y, z, barycentric, texCoord, normal, worldPosition);
                }
            }
        }
    }

    /**
     * Интерфейс callback для обработки пикселей при растеризации.
     */
    public interface PixelCallback {
        /**
         * Вызывается для каждого пикселя внутри треугольника.
         *
         * @param x, y координаты пикселя в экранных координатах
         * @param z интерполированная глубина (для Z-буфера)
         * @param barycentric барицентрические координаты [alpha, beta, gamma]
         * @param texCoord интерполированные текстурные координаты (может быть null)
         * @param normal интерполированная нормаль (может быть null)
         * @param worldPosition позиция текущей вершины в мировой системе координат
         */
        void onPixel(int x, int y, float z, float[] barycentric, Vector2f texCoord, Vector3f normal, Vector3f worldPosition);
    }
}


