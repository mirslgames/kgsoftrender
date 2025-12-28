package com.cgvsu.modelOperations;

/**
 * Алгоритм Z-буфера (буфера глубины) для правильной отрисовки 3D сцены.
 */
public class ZBuffer {

    private float[][] buffer;
    private final int width;
    private final int height;

    /**
     * Создаёт Z-буфер заданного размера.
     * 
     * @param width ширина буфера (ширина экрана)
     * @param height высота буфера (высота экрана)
     */
    public ZBuffer(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        
        this.width = width;
        this.height = height;
        this.buffer = new float[height][width];
        clear();
    }

    /**
     * Очищает Z-буфер, устанавливая все значения в максимальную глубину (дальше всего от камеры).
     * Должен вызываться перед началом рендеринга каждого кадра.
     */
    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer[y][x] = Float.MAX_VALUE;
            }
        }
    }

    /**
     * Проверяет и обновляет Z-буфер для пикселя.
     * Пиксель должен быть отрисован только если он ближе к камере, чем уже сохранённый пиксель.
     * 
     * @param x, y координаты пикселя
     * @param z глубина пикселя (меньше значение = ближе к камере)
     * @return true если пиксель должен быть отрисован (он ближе к камере), false если он закрыт другим пикселем
     */
    public boolean testAndSet(int x, int y, float z) {
        // Проверяем границы
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }
        
        // В нашей системе координат чем меньше z, тем ближе пиксель
        if (z < buffer[y][x]) {
            buffer[y][x] = z;
            return true;
        }
        
        return false;
    }

    /**
     * @param x, y координаты пикселя
     * @return глубина пикселя, или Float.MAX_VALUE если пиксель не был отрисован, можно сделать null, чтобы не
     * перегружать память
     */
    public float getDepth(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Float.MAX_VALUE;
        }
        
        return buffer[y][x];
    }

    /**

     * Без проверки устанавливает глубину пикселя, будет использоваться при наложении бликов по идее
     * @param x, y координаты пикселя
     * @param z глубина пикселя
     */
    public void setDepth(int x, int y, float z) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            buffer[y][x] = z;
        }
    }

    /**
     * Возвращает ширину Z-буфера.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Возвращает высоту Z-буфера.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Проверяет координату на границы буфера
     * 
     * @param x, y координаты для проверки
     * @return true если координаты правильные
     */
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}

