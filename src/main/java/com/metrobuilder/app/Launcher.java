package com.metrobuilder.app;

/**
 * Launcher-Klasse, die als Einstiegspunkt für die JVM dient.
 * Dies ist ein bekannter JavaFX-Workaround: Da diese Klasse nicht
 * von javafx.application.Application erbt, beschwert sich Java 11+
 * nicht über fehlende Module auf dem Classpath. Sie ruft dann intern
 * die eigentliche Main-Klasse auf.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
