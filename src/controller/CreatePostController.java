package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import model.Video;
import service.FileService;
import service.PostService;

import java.io.File;

public class CreatePostController {

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    
    private File fileVideo;
    private File fileThumb;

    private PostService postService;
    private FileService fileService;

    @FXML
    private void selectVideo() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vídeos", "*.mp4"));
        this.fileVideo = fc.showOpenDialog(null);
    }

    @FXML
    private void selectThumbnail() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png"));
        this.fileThumb = fc.showOpenDialog(null);
    }

    @FXML
    private void aoClicarPublicar() {
        if (fileVideo == null || fileThumb == null) {
            System.out.println("Selecione todos os files!");
            return;
        }

        // Criamos o objeto modelo com os dados da tela
        
        Video newVideo = new Video();
        newVideo.setTitle(txtTitle.getText());
        newVideo.setDescription(txtDescription.getText());
        newVideo.setChannelId(1L); // Exemplo: ID do canal logado

        // Iniciamos o processo de extrair duração e salvar
        processarDuracaoESalvar(fileVideo, newVideo);
    }

    private void processarDuracaoESalvar(File file, Video video) {
        try {
            // 1. Criamos o Media para ler o file
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            // 2. O JavaFX avisa quando terminar de ler o file (OnReady)
            mediaPlayer.setOnReady(() -> {
                try {
                    // Pegar duração em segundos
                    int sec = (int) media.getDuration().toSeconds();
                    video.setDurationSeconds(sec);

                    // 3. Agora que temos a duração, salvamos os files físicos no HD
                    String pathVideo = fileService.saveFile(fileVideo, "VIDEO");
                    String pathThumb = fileService.saveFile(fileThumb, "THUMBNAIL");

                    // 4. Setamos os caminhos retornados no objeto
                    video.setVideoUrl(pathVideo);
                    video.setThumbnailUrl(pathThumb);

                    // 5. Chamamos o Service para gravar no Banco (MySQL)
                    postService.publishPost(video);

                    System.out.println("Vídeo publicado com sucesso! Duração: " + sec + "s");
                    
                    // Limpar recursos
                    mediaPlayer.dispose();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Caso o file esteja corrompido ou formato inválido
            mediaPlayer.setOnError(() -> {
                System.err.println("Erro ao ler file de vídeo: " + mediaPlayer.getError().getMessage());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}