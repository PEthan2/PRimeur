import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAO {

    public List<Fournisseur> getAll() throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur ORDER BY nom ASC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Fournisseur getById(int id) throws SQLException {
        String sql = "SELECT * FROM fournisseur WHERE id_fournisseur = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Fournisseur f) throws SQLException {
        String sql = "INSERT INTO fournisseur (nom, telephone, email, adresse) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getTelephone());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getAdresse());
            ps.executeUpdate();
        }
    }

    public void update(Fournisseur f) throws SQLException {
        String sql = "UPDATE fournisseur SET nom=?, telephone=?, email=?, adresse=? WHERE id_fournisseur=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getTelephone());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getAdresse());
            ps.setInt   (5, f.getIdFournisseur());
            ps.executeUpdate();
        }
    }

    public boolean delete(int idFournisseur) throws SQLException {
        String check = "SELECT COUNT(*) FROM article WHERE id_fournisseur = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(check)) {
            ps.setInt(1, idFournisseur);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
        }
        String sql = "DELETE FROM fournisseur WHERE id_fournisseur = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idFournisseur);
            ps.executeUpdate();
        }
        return true;
    }

    private Fournisseur map(ResultSet rs) throws SQLException {
        return new Fournisseur(
            rs.getInt   ("id_fournisseur"),
            rs.getString("nom"),
            rs.getString("telephone"),
            rs.getString("email"),
            rs.getString("adresse")
        );
    }
}
