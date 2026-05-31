import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {

    public List<Article> getAll(String orderBy, boolean asc) throws SQLException {
        List<Article> list = new ArrayList<>();

        String col = switch (orderBy) {
            case "prix_unitaire"  -> "a.prix_unitaire";
            case "quantite_stock" -> "a.quantite_stock";
            default               -> "a.nom";
        };
        String direction = asc ? "ASC" : "DESC";

        String sql = "SELECT a.*, f.nom AS nom_fournisseur "
                   + "FROM article a "
                   + "LEFT JOIN fournisseur f ON a.id_fournisseur = f.id_fournisseur "
                   + "ORDER BY " + col + " " + direction;

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Article a = map(rs);
                a.setNomFournisseur(rs.getString("nom_fournisseur"));
                list.add(a);
            }
        }
        return list;
    }

    public List<Article> getAll() throws SQLException {
        return getAll("nom", true);
    }

    public void insert(Article a) throws SQLException {
        String sql = "INSERT INTO article (nom, type, prix_unitaire, quantite_stock, id_fournisseur) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getType());
            ps.setDouble(3, a.getPrixUnitaire());
            ps.setInt   (4, a.getQuantiteStock());
            ps.setInt   (5, a.getIdFournisseur());
            ps.executeUpdate();
        }
    }

    public void update(Article a) throws SQLException {
        String sql = "UPDATE article SET nom=?, type=?, prix_unitaire=?, quantite_stock=?, id_fournisseur=? "
                   + "WHERE id_article=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getType());
            ps.setDouble(3, a.getPrixUnitaire());
            ps.setInt   (4, a.getQuantiteStock());
            ps.setInt   (5, a.getIdFournisseur());
            ps.setInt   (6, a.getIdArticle());
            ps.executeUpdate();
        }
    }

    public void delete(int idArticle) throws SQLException {
        String sql = "DELETE FROM article WHERE id_article=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idArticle);
            ps.executeUpdate();
        }
    }

    private Article map(ResultSet rs) throws SQLException {
        Article a = new Article();
        a.setIdArticle    (rs.getInt   ("id_article"));
        a.setNom          (rs.getString("nom"));
        a.setType         (rs.getString("type"));
        a.setPrixUnitaire (rs.getDouble("prix_unitaire"));
        a.setQuantiteStock(rs.getInt   ("quantite_stock"));
        a.setIdFournisseur(rs.getInt   ("id_fournisseur"));
        return a;
    }
}
