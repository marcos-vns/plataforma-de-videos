package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileService {

    // Define onde os arquivos ficarão. "." significa a pasta raiz do projeto.
    private static final String DIRETORIO_BASE = "storage"; 
    private static final String PASTA_VIDEOS = "videos";
    private static final String PASTA_THUMBNAILS = "thumbnails";
    private static final String PASTA_PROFILES = "profiles";

    public FileService() {
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(DIRETORIO_BASE, PASTA_VIDEOS));
            Files.createDirectories(Paths.get(DIRETORIO_BASE, PASTA_THUMBNAILS));
            Files.createDirectories(Paths.get(DIRETORIO_BASE, PASTA_PROFILES));
        } catch (IOException e) {
            e.printStackTrace(); // Trate com logs adequados
        }
    }

    /**
     * Salva o arquivo e retorna o caminho relativo para salvar no banco.
     * @param originalFile O arquivo vindo do FileChooser do JavaFX
     * @param type "VIDEO" ou "THUMBNAIL"
     * @return O caminho relativo (ex: videos/uuid.mp4)
     */
    public String saveFile(File originalFile, String type) throws IOException {
        if (originalFile == null) return null;

        // 1. Gerar nome único
        String extension = type.equals("VIDEO") ? ".mp4" : getExtension(originalFile.getName());
        String newName = UUID.randomUUID().toString() + extension;

        // 2. Definir pasta de destino
        String subFolder;
        switch (type) {
            case "VIDEO" -> subFolder = PASTA_VIDEOS;
            case "PROFILE" -> subFolder = PASTA_PROFILES;
            default -> subFolder = PASTA_THUMBNAILS;
        }
        Path destination = Paths.get(DIRETORIO_BASE, subFolder, newName);

        // 3. Processar ou copiar o arquivo
        if (type.equals("VIDEO")) {
            convertVideo(originalFile, destination.toFile());
        } else {
            Files.copy(originalFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        }

        // 4. Retornar o caminho relativo (para salvar no banco)
        return subFolder + File.separator + newName;
    }

    private void convertVideo(File input, File output) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-i", input.getAbsolutePath(),
            "-c:v", "libx264", "-profile:v", "high", "-pix_fmt", "yuv420p",
            "-c:a", "aac", "-movflags", "+faststart",
            "-y", // Sobrescrever se existir
            output.getAbsolutePath()
        );
        
        pb.inheritIO(); // Redireciona log para o console do Java
        
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg falhou com código: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Conversão interrompida", e);
        }
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index);
        }
        return ""; // Sem extensão (raro)
    }
}