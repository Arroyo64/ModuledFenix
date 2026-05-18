package org.ies.fenix.server.utils;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class LauncherRpyBuilder {

    private final File contentRoot;

    private final String baseProjectsDir = "uploads/projects/";
    private final String outputPath = "uploads/game";
    private final String configPath = "server/src/main/resources/renconstruct.toml";
    private final String renpyVersion = "8.5.2";

    private final File renutilExe;
    private final File renconstructExe;

    private String projectName;
    private File logFile;

    public LauncherRpyBuilder(String projectName) {
        this.contentRoot = new File("").getAbsoluteFile();
        this.projectName = projectName;

        String cargoBin = System.getProperty("user.home") + "\\.cargo\\bin";

        this.renutilExe = new File(cargoBin, "renutil.exe");
        this.renconstructExe = new File(cargoBin, "renconstruct.exe");

        validateProject();
    }

    // -------------------------------
    // VALIDACIÓN
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
        if (!toml.exists()) {
            throw new IllegalArgumentException("No existe config: " + toml.getPath());
        }

        new File(contentRoot, outputPath).mkdirs();
    }

    // -------------------------------
    // LOG
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
    // EJECUTAR PROCESOS (ABSOLUTO)
    // -------------------------------
    private int runProcess(File exe, String... args) throws IOException, InterruptedException {

        if (!exe.exists()) {
            throw new RuntimeException("No existe ejecutable: " + exe.getAbsolutePath());
        }

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(buildCommand(exe.getAbsolutePath(), args));
        pb.directory(contentRoot);
        pb.redirectErrorStream(true);

        Process process = pb.start();

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

    private String[] buildCommand(String exe, String... args) {
        String[] cmd = new String[args.length + 1];
        cmd[0] = exe;
        System.arraycopy(args, 0, cmd, 1, args.length);
        return cmd;
    }

    // -------------------------------
    // INSTALAR RENKIT SI NO EXISTE
    // -------------------------------
    private void installRenkitIfNeeded() throws IOException, InterruptedException {

        if (renutilExe.exists() && renconstructExe.exists()) {
            writeLog("RenKit ya instalado");
            return;
        }

        writeLog("Instalando RenKit...");

        String cmd =
                "powershell -ExecutionPolicy Bypass -c " +
                        "\"irm https://github.com/kobaltcore/renkit/releases/download/v6.1.0/renkit-installer.ps1 | iex\"";

        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", cmd);
        pb.inheritIO();

        int code = pb.start().waitFor();

        if (code != 0) {
            throw new RuntimeException("Error instalando RenKit");
        }

        writeLog("RenKit instalado");
    }

    // -------------------------------
    // RENUTIL CHECK (SIN CMD)
    // -------------------------------
    private boolean isRenpyInstalled() throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                renutilExe.getAbsolutePath(),
                "list"
        );

        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(renpyVersion)) {
                    return true;
                }
            }
        }

        p.waitFor();
        return false;
    }

    // -------------------------------
    // INSTALL RENPY
    // -------------------------------
    public void ensureRenpyInstalled() throws IOException, InterruptedException {

        installRenkitIfNeeded();

        if (isRenpyInstalled()) {
            writeLog("Ren'Py ya instalado");
            return;
        }

        writeLog("Instalando Ren'Py...");

        int code = runProcess(renutilExe, "install", renpyVersion);

        if (code != 0) {
            throw new RuntimeException("Error instalando Ren'Py");
        }
    }

    // -------------------------------
    // BUILD
    // -------------------------------
    public void buildWindows() throws IOException, InterruptedException {

        String inputDir = baseProjectsDir + projectName;

        writeLog("Ejecutando build...");

        int code = runProcess(
                renconstructExe,
                "build",
                "-c",
                configPath,
                inputDir,
                outputPath
        );

        if (code != 0) {
            throw new RuntimeException("Error construyendo build");
        }

        writeLog("Build completada");
    }

    // -------------------------------
    // FLUJO
    // -------------------------------
    public void run() throws IOException, InterruptedException {
        ensureRenpyInstalled();
        buildWindows();
    }

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