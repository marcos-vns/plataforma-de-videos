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

    public FileService() {
        createDirectories();
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(DIRETORIO_BASE, PASTA_VIDEOS));
            Files.createDirectories(Paths.get(DIRETORIO_BASE, PASTA_THUMBNAILS));
        } catch (IOException e) {
            e.printStackTrace(); // Trate com logs adequados
        }
    }

    /**
     * Salva o arquivo e retorna o caminho relativo para salvar no banco.
     * @param arquivoOriginal O arquivo vindo do FileChooser do JavaFX
     * @param tipo "VIDEO" ou "THUMBNAIL"
     * @return O caminho relativo (ex: videos/uuid.mp4)
     */
    public String saveFile(File arquivoOriginal, String tipo) throws IOException {
        if (arquivoOriginal == null) return null;

        // 1. Gerar nome único
        String extensao = getExtension(arquivoOriginal.getName());
        String novoNome = UUID.randomUUID().toString() + extensao;

        // 2. Definir pasta de destino
        String subPasta = tipo.equals("VIDEO") ? PASTA_VIDEOS : PASTA_THUMBNAILS;
        Path destino = Paths.get(DIRETORIO_BASE, subPasta, novoNome);

        // 3. Copiar o arquivo
        Files.copy(arquivoOriginal.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        // 4. Retornar o caminho relativo (para salvar no banco)
        // No Windows usará backslash (\), no Linux slash (/). 
        // É bom padronizar para / se for exibir na web depois, mas localmente o File separator está ok.
        return subPasta + File.separator + novoNome;
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index);
        }
        return ""; // Sem extensão (raro)
    }
}