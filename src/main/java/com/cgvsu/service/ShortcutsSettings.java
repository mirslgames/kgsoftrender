package com.cgvsu.service;

import java.util.HashMap;

public class ShortcutsSettings {
    public static String openModel = "CTRL + F";
    public static String saveModel = "CTRL + S";

    public static String getInfo(){
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%s  =  Загрузить модель\n", openModel));
        sb.append(String.format("%s  =  Сохранить модель\n", saveModel));

        return sb.toString();
    }
}
