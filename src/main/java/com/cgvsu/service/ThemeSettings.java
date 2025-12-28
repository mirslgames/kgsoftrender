package com.cgvsu.service;

public class ThemeSettings {

    public static String rootStyle;
    public static String paneStyle;

    public static String splitPaneStyle;
    public static String splitDividerStyle;

    public static String menuBarStyle;
    public static String menuBarLabelStyle;

    public static String labelStyle;
    public static String checkBoxStyle;
    public static String textFieldStyle;

    public static String buttonStyle;
    public static String activeButtonStyle;

    public static String titledPaneTitleStyle;
    public static String titledPaneTitleTextStyle;
    public static String titledPaneContentStyle;

    public static String scrollPaneStyle;
    public static String scrollPaneViewportStyle;

    public static String scrollBarStyle;
    public static String scrollBarThumbStyle;

    public static String wireframeColor;
    public static double wireframeWidth;
    public static String canvasBackgroundColor;


    public static void setLightTheme() {
        rootStyle = "-fx-background-color: #EEF2F7;";
        paneStyle = "-fx-background-color: #EEF2F7;";

        splitPaneStyle = "-fx-background-color: #EEF2F7;";
        splitDividerStyle =
                "-fx-background-color: #B9C6D6;" +
                        "-fx-padding: 0.8;" +
                        "-fx-background-insets: 0;";

        menuBarStyle =
                "-fx-background-color: #FFFFFF;" +
                        "-fx-border-color: transparent transparent #CBD5E1 transparent;" +
                        "-fx-border-width: 0 0 1 0;";
        menuBarLabelStyle = "-fx-text-fill: #111827;";

        labelStyle = "-fx-text-fill: #111827;";
        checkBoxStyle =
                "-fx-text-fill: #111827;" +
                        "-fx-mark-color: #2563EB;" +
                        "-fx-mark-highlight-color: transparent;" +
                        "-fx-shadow-highlight-color: transparent;";
        textFieldStyle =
                "-fx-control-inner-background: #FFFFFF;" +
                        "-fx-text-fill: #111827;" +
                        "-fx-prompt-text-fill: #6B7280;" +
                        "-fx-border-color: #CBD5E1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;";

        buttonStyle =
                "-fx-background-color: #F8FAFC;" +
                        "-fx-text-fill: #111827;" +
                        "-fx-border-color: #CBD5E1;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;";
        activeButtonStyle =
                "-fx-background-color: #BFDBFE;" +
                        "-fx-text-fill: #0F172A;" +
                        "-fx-border-color: #60A5FA;";

        titledPaneTitleStyle =
                "-fx-background-color: #FFFFFF;" +
                        "-fx-border-color: #B9C6D6;" +
                        "-fx-border-width: 1;";
        titledPaneTitleTextStyle = "-fx-fill: #111827;";
        titledPaneContentStyle =
                "-fx-background-color: #FFFFFF;" +
                        "-fx-border-color: #B9C6D6;" +
                        "-fx-border-width: 0 1 1 1;";
        scrollPaneStyle =
                "-fx-background-color: #FFFFFF;" +
                        "-fx-border-color: #B9C6D6;" +
                        "-fx-border-width: 1;";
        scrollPaneViewportStyle = "-fx-background-color: #FFFFFF;";

        scrollBarStyle = "-fx-background-color: transparent;";
        scrollBarThumbStyle = "-fx-background-color: #CBD5E1;";

        wireframeColor = "#111827";
        wireframeWidth = 1.0;
        canvasBackgroundColor = "#F8FAFC";
    }

    public static void setDarkTheme() {
        rootStyle = "-fx-background-color: #151A20;";
        paneStyle = "-fx-background-color: #151A20;";

        splitPaneStyle = "-fx-background-color: #151A20;";
        splitDividerStyle =
                "-fx-background-color: #334155;" +
                        "-fx-padding: 1.0;" +
                        "-fx-background-insets: 0;";

        menuBarStyle =
                "-fx-background-color: #1B232D;" +
                        "-fx-border-color: transparent transparent #2A3440 transparent;" +
                        "-fx-border-width: 0 0 1 0;";
        menuBarLabelStyle = "-fx-text-fill: #E5E7EB;";

        labelStyle = "-fx-text-fill: #E5E7EB;";
        checkBoxStyle =
                "-fx-text-fill: #DCE7F7;" +
                        "-fx-mark-color: #2563EB;" +
                        "-fx-mark-highlight-color: transparent;" +
                        "-fx-shadow-highlight-color: transparent;";


        textFieldStyle =
                "-fx-control-inner-background: #1F2A36;" +
                        "-fx-text-fill: #E5E7EB;" +
                        "-fx-prompt-text-fill: #94A3B8;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;";

        buttonStyle =
                "-fx-background-color: #223041;" +
                        "-fx-text-fill: #E5E7EB;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 4;" +
                        "-fx-background-radius: 4;";
        activeButtonStyle =
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-color: #60A5FA;";

        titledPaneTitleStyle =
                "-fx-background-color: #1B232D;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 1;";
        titledPaneTitleTextStyle = "-fx-fill: #E5E7EB;";
        titledPaneContentStyle =
                "-fx-background-color: #1A222C;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 0 1 1 1;";

        scrollPaneStyle =
                "-fx-background-color: #1A222C;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 1;";
        scrollPaneViewportStyle = "-fx-background-color: #1A222C;";

        scrollBarStyle = "-fx-background-color: transparent;";
        scrollBarThumbStyle = "-fx-background-color: #334155;";

        canvasBackgroundColor = "#121C26";
        wireframeColor = "rgba(220, 235, 255, 0.95)";
        wireframeWidth = 1.05;
    }
}
