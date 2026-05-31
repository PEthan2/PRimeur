import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour les fournisseurs.
 *
 * Toutes les opérations SQL sur la table "fournisseur" passent par cette classe.
 * Opérations disponibles : lire tous, lire par ID, insérer, modifier, supprimer.
 */
public class FournisseurDAO {

    /**
     * Retourne la liste de tous les fournisseurs, triés par nom alphabétique.
     */
    public List<Fournisseur> getAll() throws SQLException {
        List<Fournisseur> list = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur ORDER BY nom ASC";
        // try-with-resources : ferme automatiquement Statement et ResultSet
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs)); // Convertit chaque ligne SQL en objet Fournisseur
            }
        }
        return list;
    }

    /**
     * Cherche un fournisseur par son identifiant unique.
     * Retourne null si aucun fournisseur ne correspond à cet ID.
     */
    public Fournisseur getById(int id) throws SQLException {
        String sql = "SELECT * FROM fournisseur WHERE id_fournisseur = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs); // Retourne le fournisseur trouvé
            }
        }
        return null; // Aucun fournisseur avec cet ID
    }

    /**
     * Insère un nouveau fournisseur en base de données.
     * L'id_fournisseur est géré automatiquement par MySQL (AUTO_INCREMENT).
     */
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

    /**
     * Met à jour les informations d'un fournisseur existant.
     */
    public void update(Fournisseur f) throws SQLException {
        String sql = "UPDATE fournisseur SET nom=?, telephone=?, email=?, adresse=? WHERE id_fournisseur=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, f.getNom());
            ps.setString(2, f.getTelephone());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getAdresse());
            ps.setInt   (5, f.getIdFournisseur()); // Critère WHERE
            ps.executeUpdate();
        }
    }

    /**
     * Supprime un fournisseur SEULEMENT s'il n'a aucun article associé.
     *
     * @return true si la suppression a réussi, false si des articles lui sont encore liés.
     *
     * Cette vérification évite de violer la contrainte d'intégrité référentielle
     * (un article ne peut pas pointer vers un fournisseur inexistant).
     */
    public boolean delete(int idFournisseur) throws SQLException {
        // Vérifie d'abord si des articles utilisent ce fournisseur
        String check = "SELECT COUNT(*) FROM article WHERE id_fournisseur = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(check)) {
            ps.setInt(1, idFournisseur);
            try (ResultSet rs = ps.executeQuery()) {
                // Si au moins un article est lié, on refuse la suppression
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
        }
        // Aucun article lié : on peut supprimer
        String sql = "DELETE FROM fournisseur WHERE id_fournisseur = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idFournisseur);
            ps.executeUpdate();
        }
        return true;
    }

    /**
     * Transforme une ligne de ResultSet en objet Fournisseur.
     * Méthode privée réutilisée par getAll() et getById().
     */
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
