package daos;

import java.sql.*;

import database.DatabaseConnection;
import model.Post;
import model.TextPost;
import model.Video;

public class PostDAO {

    public void save(Post post) throws SQLException {
        Connection conn = null;
        PreparedStatement psPost = null;
        PreparedStatement psChild = null;

        String sql = "INSERT INTO posts (canal_id, titulo, thumbnail_url, tipo_post, data_criacao) VALUES (?, ?, ?, ?, NOW())";
        
        try {
            // 1. Obter conexão e INICIAR TRANSAÇÃO
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Desliga o commit automático

            // 2. Inserir na tabela pai (POSTS)
            // Statement.RETURN_GENERATED_KEYS é obrigatório para pegar o ID criado
            psPost = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            psPost.setLong(1, post.getChannelId());
            psPost.setString(2, post.getTitle());
            psPost.setString(3, post.getThumbnailUrl());
            psPost.setString(4, post.getTipoPost().name()); // Salva "VIDEO" ou "TEXTO"
            
            int affectedRows = psPost.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Falha ao criar post, nenhuma linha afetada.");
            }

            // 3. Recuperar o ID gerado (Obrigatório para a FK)
            try (ResultSet generatedKeys = psPost.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Falha ao criar post, nenhum ID obtido.");
                }
            }

            // 4. Inserir na tabela filha específica (Polimorfismo)
            if (post instanceof Video) {
                salvarVideo((Video) post, conn);
            } else if (post instanceof TextPost) {
                salvarTexto((TextPost) post, conn);
            }

            // 5. EFETIVAR TRANSAÇÃO (Se chegou aqui, tudo deu certo)
            conn.commit();

        } catch (SQLException e) {
            // 6. ROLLBACK (Se der erro, desfaz tudo)
            if (conn != null) {
                try {
                    System.err.println("Erro na transação. Desfazendo alterações: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Relança o erro para o Service saber que falhou
        } finally {
            // 7. Fechar recursos (Boas práticas)
            if (psPost != null) psPost.close();
            if (psChild != null) psChild.close();
            if (conn != null) {
                conn.setAutoCommit(true); // Restaura o estado padrão
                conn.close(); // Devolve para o pool do Hikari
            }
        }
    }

    // Métodos auxiliares privados recebendo a conexão ABERTA
    private void salvarVideo(Video video, Connection conn) throws SQLException {
        String sql = "INSERT INTO videos (post_id, descricao, duracao_segundos, video_url, categoria_video) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, video.getId()); // FK é o ID do pai
            ps.setString(2, video.getDescription());
            ps.setInt(3, video.getDurationSeconds());
            ps.setString(4, video.getVideoUrl());
            ps.setString(5, video.getCategory().name()); // "LONGO" ou "SHORT"
            
            ps.executeUpdate();
        }
    }

    private void salvarTexto(TextPost text, Connection conn) throws SQLException {
        String sql = "INSERT INTO posts_texto (post_id, conteudo) VALUES (?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, text.getId()); // FK é o ID do pai
            ps.setString(2, text.getContent());
            
            ps.executeUpdate();
        }
    }
}
