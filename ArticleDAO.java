import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) pour les articles.
 *
 * Toutes les opérations SQL sur la table "article" passent par cette classe.
 * Cela sépare la logique d'accès aux données du reste de l'application
 * (pattern DAO).
 *
 * Opérations disponibles : lire tous les articles, insérer, modifier, supprimer.
 */
public class ArticleDAO {

    /**
     * Retourne tous les articles triés selon la colonne et le sens choisis.
     *
     * @param orderBy  Nom du champ de tri : "nom", "prix_unitaire" ou "quantite_stock"
     * @param asc      true = ordre croissant (ASC), false = ordre décroissant (DESC)
     */
    public List<Article> getAll(String orderBy, boolean asc) throws SQLException {
        List<Article> list = new ArrayList<>();

        // Traduit le nom de tri (choisi dans l'interface) vers le vrai nom de colonne SQL.
        // Le switch évite d'injecter directement une chaîne utilisateur dans la requête.
        String col = switch (orderBy) {
            case "prix_unitaire"  -> "a.prix_unitaire";
            case "quantite_stock" -> "a.quantite_stock";
            default               -> "a.nom";
        };
        String direction = asc ? "ASC" : "DESC";

        // JOIN avec la table fournisseur pour récupérer le nom du fournisseur en une seule requête.
        // LEFT JOIN : affiche aussi les articles sans fournisseur (nom_fournisseur = NULL dans ce cas).
        String sql = "SELECT a.*, f.nom AS nom_fournisseur "
                   + "FROM article a "
                   + "LEFT JOIN fournisseur f ON a.id_fournisseur = f.id_fournisseur "
                   + "ORDER BY " + col + " " + direction;

        // try-with-resources : ferme automatiquement Statement et ResultSet à la fin du bloc
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Article a = map(rs);                              // Remplit un objet Article depuis la ligne SQL
                a.setNomFournisseur(rs.getString("nom_fournisseur")); // Ajout du nom issu du JOIN
                list.add(a);
            }
        }
        return list;
    }

    /**
     * Surcharge de commodité : retourne tous les articles triés par nom croissant.
     */
    public List<Article> getAll() throws SQLException {
        return getAll("nom", true);
    }

    /**
     * Insère un nouvel article en base de données.
     * Utilise un PreparedStatement pour éviter les injections SQL.
     */
    public void insert(Article a) throws SQLException {
        String sql = "INSERT INTO article (nom, type, prix_unitaire, quantite_stock, id_fournisseur) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getType());
            ps.setDouble(3, a.getPrixUnitaire());
            ps.setInt   (4, a.getQuantiteStock());
            ps.setInt   (5, a.getIdFournisseur());
            ps.executeUpdate(); // Exécute l'INSERT
        }
    }

    /**
     * Met à jour un article existant (identifié par son id_article).
     */
    public void update(Article a) throws SQLException {
        String sql = "UPDATE article SET nom=?, type=?, prix_unitaire=?, quantite_stock=?, id_fournisseur=? "
                   + "WHERE id_article=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getType());
            ps.setDouble(3, a.getPrixUnitaire());
            ps.setInt   (4, a.getQuantiteStock());
            ps.setInt   (5, a.getIdFournisseur());
            ps.setInt   (6, a.getIdArticle()); // Critère WHERE
            ps.executeUpdate();
        }
    }

    /**
     * Supprime un article par son identifiant.
     */
    public void delete(int idArticle) throws SQLException {
        String sql = "DELETE FROM article WHERE id_article=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idArticle);
            ps.executeUpdate();
        }
    }

    /**
     * Transforme une ligne de ResultSet en objet Article.
     * Méthode privée utilisée uniquement dans ce DAO pour éviter la duplication de code.
     */
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
