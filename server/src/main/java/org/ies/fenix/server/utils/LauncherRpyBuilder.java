package org.ies.fenix.server.utils;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class LauncherRpyBuilder {

    private final File contentRoot;

    private final String baseProjectsDir = "uploads/projects/";
    private final String outputPath = "uploads/game";
    private final String configPath = "server/src/main/resources/renconstruct.toml";
    private final String renpyVersion = "8.5.2";

    private String projectName;
    private File logFile;

    public LauncherRpyBuilder(String projectName) {
        // Forzar content root al módulo server
        this.contentRoot = new File("").getAbsoluteFile();
        this.projectName = projectName;
        validateProject();
    }

    // -------------------------------
    // Validar proyecto
    // -------------------------------
    private void validateProject() {
        File projectDir = new File(contentRoot, baseProjectsDir + projectName);
        if (!projectDir.exists()) {
            throw new IllegalArgumentException("El proyecto no existe: " + projectDir.getPath());
        }

        File gameDir = new File(projectDir, "game");
        if (!gameDir.exists()) {
            throw new IllegalArgumentException("El proyecto no contiene carpeta 'game': " + gameDir.getPath());
        }

        File toml = new File(contentRoot, configPath);
        System.out.println("toml = " + toml.getPath());

        if (!toml.exists()) {
            throw new IllegalArgumentException("No existe el archivo de configuración: " + toml.getPath());
        }

        File out = new File(contentRoot, outputPath);
        if (!out.exists()) {
            out.mkdirs();
        }
    }

    // -------------------------------
    // Validar TOML
    // -------------------------------
    public boolean validateToml() {
        File toml = new File(contentRoot, configPath);
        try (BufferedReader br = new BufferedReader(new FileReader(toml))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("[")) return true;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // -------------------------------
    // Log a archivo
    // -------------------------------
    public void logToFile(String logPath) {
        this.logFile = new File(contentRoot, logPath);
        this.logFile.getParentFile().mkdirs();
    }

    private void writeLog(String text) {
        if (logFile == null) return;
        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write(text + System.lineSeparator());
        } catch (IOException ignored) {}
    }

    // -------------------------------
    // Ejecutar comandos
    // -------------------------------
    private int runCommand(String command, File workingDir) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
        builder.directory(workingDir);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RENKIT] " + line);
                writeLog(line);
            }
        }

        return process.waitFor();
    }

    // -------------------------------
    // Verificar instalación Ren'Py
    // -------------------------------
    private boolean isRenpyInstalled() throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "renutil list");
        builder.directory(contentRoot);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(renpyVersion)) {
                    return true;
                }
            }
        }

        process.waitFor();
        return false;
    }

    // -------------------------------
    // Instalar Ren'Py si falta
    // -------------------------------
    public void ensureRenpyInstalled() throws IOException, InterruptedException {
        if (isRenpyInstalled()) {
            writeLog("Ren'Py ya instalado");
            return;
        }

        writeLog("Instalando Ren'Py...");
        int code = runCommand("renutil install " + renpyVersion, contentRoot);

        if (code != 0) {
            throw new RuntimeException("Error instalando Ren'Py");
        }
    }

    // -------------------------------
    // Build Windows
    // -------------------------------
    public void buildWindows() throws IOException, InterruptedException {

        String inputDir = baseProjectsDir + projectName;

        String cmd = String.format(
                "renconstruct build -c \"%s\" \"%s\" \"%s\"",
                configPath, inputDir, outputPath
        );

        writeLog("Ejecutando build...");
        int code = runCommand(cmd, contentRoot);

        if (code != 0) {
            throw new RuntimeException("Error construyendo la build");
        }

        writeLog("Build completada");
    }

    // -------------------------------
    // Flujo completo
    // -------------------------------
    public void run() throws IOException, InterruptedException {
        ensureRenpyInstalled();
        buildWindows();
    }

    // -------------------------------
    // Ejecución asíncrona
    // -------------------------------
    public CompletableFuture<Void> runAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                run();
            } catch (Exception e) {
                writeLog("ERROR: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}
