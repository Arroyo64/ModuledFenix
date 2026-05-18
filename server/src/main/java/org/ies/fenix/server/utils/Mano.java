package org.ies.fenix.server.utils;

public class Mano {
    public static void main(String[] args) {
        try {
            LauncherRpyBuilder builder = new LauncherRpyBuilder("renpy-template-master");
            builder.logToFile("logs/build-renpy-template-master.txt");
            builder.runAsync().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}