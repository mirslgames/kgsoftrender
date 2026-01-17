package com.cgvsu.objwriter;

public class ObjWriterException extends RuntimeException {
    public ObjWriterException(String errorMessage) {
        super("Ошибка записи в OBJ файл: " + " " + errorMessage);
    }
}
