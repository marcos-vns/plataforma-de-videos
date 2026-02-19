package controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.PostType;
import model.TextPost;
import model.Video;
import service.FileService;
import service.PostService;
import service.ChannelSession;

import java.io.File;

public class CreatePostController {

    @FXML private ComboBox<PostType> comboPostType;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private TextArea txtContent;
    @FXML private VBox videoSection;
    @FXML private VBox textSection;
    @FXML private Label lblVideoStatus;
    @FXML private Label lblThumbStatus;
    @FXML private Label lblStatus;
    
    private File fileVideo;
    private File fileThumb;

    private PostService postService;
    private FileService fileService;
    
    private Task<Void> activeTask;

    public void setPostService(PostService postService) {
        this.postService = postService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @FXML
    public void initialize() {
        comboPostType.getItems().setAll(PostType.values());
        comboPostType.setValue(PostType.VIDEO);
    }

    @FXML
    private void onTypeChange() {
        boolean isVideo = comboPostType.getValue() == PostType.VIDEO;
        videoSection.setVisible(isVideo);
        videoSection.setManaged(isVideo);
        textSection.setVisible(!isVideo);
        textSection.setManaged(!isVideo);
        lblStatus.setText("");
    }

    @FXML
    private void selectVideo() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Vídeos", "*.mp4"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            this.fileVideo = file;
            lblVideoStatus.setText(file.getName());
        }
    }

    @FXML
    private void selectThumbnail() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            this.fileThumb = file;
            lblThumbStatus.setText(file.getName());
        }
    }

    @FXML
    private void aoClicarPublicar() {
        if (activeTask != null && activeTask.isRunning()) {
            return;
        }

        String title = txtTitle.getText();
        if (title == null || title.trim().isEmpty()) {
            lblStatus.setText("O título é obrigatório.");
            return;
        }

        if (!ChannelSession.hasSelectedChannel()) {
            lblStatus.setText("Erro: Nenhum canal selecionado.");
            return;
        }

        if (comboPostType.getValue() == PostType.VIDEO) {
            publishVideo();
        } else {
            publishText();
        }
    }

    private void publishVideo() {
        if (fileVideo == null || fileThumb == null) {
            lblStatus.setText("Selecione o vídeo e a thumbnail!");
            return;
        }

        Video newVideo = new Video();
        newVideo.setTitle(txtTitle.getText());
        newVideo.setDescription(txtDescription.getText());
        newVideo.setChannelId(ChannelSession.getChannel().getId());
        newVideo.setDurationSeconds(0); // Simplificado: sem leitura de duração por enquanto

        activeTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Salvando arquivos...");
                String pathVideo = fileService.saveFile(fileVideo, "VIDEO");
                String pathThumb = fileService.saveFile(fileThumb, "THUMBNAIL");

                newVideo.setVideoUrl(pathVideo);
                newVideo.setThumbnailUrl(pathThumb);

                updateMessage("Gravando no banco de dados...");
                postService.publishPost(newVideo);
                return null;
            }

            @Override
            protected void succeeded() {
                lblStatus.textProperty().unbind();
                lblStatus.setText("VÍDEO PUBLICADO COM SUCESSO!");
                clearFields();
            }

            @Override
            protected void failed() {
                lblStatus.textProperty().unbind();
                lblStatus.setText("ERRO: " + getException().getMessage());
                getException().printStackTrace();
            }
        };

        lblStatus.textProperty().bind(activeTask.messageProperty());
        Thread t = new Thread(activeTask);
        t.setDaemon(true);
        t.start();
    }

    private void publishText() {
        String content = txtContent.getText();
        if (content == null || content.length() < 10) {
            lblStatus.setText("O conteúdo deve ter pelo menos 10 caracteres.");
            return;
        }

        TextPost textPost = new TextPost();
        textPost.setTitle(txtTitle.getText());
        textPost.setContent(content);
        textPost.setChannelId(ChannelSession.getChannel().getId());

        activeTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Publicando texto...");
                
                if (fileThumb != null) {
                    updateMessage("Salvando miniatura...");
                    String pathThumb = fileService.saveFile(fileThumb, "THUMBNAIL");
                    textPost.setThumbnailUrl(pathThumb);
                }

                postService.publishPost(textPost);
                return null;
            }

            @Override
            protected void succeeded() {
                lblStatus.textProperty().unbind();
                lblStatus.setText("Texto publicado!");
                clearFields();
            }

            @Override
            protected void failed() {
                lblStatus.textProperty().unbind();
                lblStatus.setText("Erro: " + getException().getMessage());
            }
        };

        lblStatus.textProperty().bind(activeTask.messageProperty());
        Thread t = new Thread(activeTask);
        t.setDaemon(true);
        t.start();
    }

    private void clearFields() {
        txtTitle.clear();
        txtDescription.clear();
        txtContent.clear();
        fileVideo = null;
        fileThumb = null;
        lblVideoStatus.setText("Nenhum arquivo");
        lblThumbStatus.setText("Nenhuma imagem");
    }

    @FXML
    private void backToStudio() {
        SceneManager.showStudioScene(ChannelSession.getChannel());
    }
}
