import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gère la connexion unique à la base de données MySQL.
 *
 * Utilise le patron de conception "Singleton" : une seule instance de Connection
 * est créée et réutilisée dans toute l'application, ce qui évite d'ouvrir
 * plusieurs connexions inutiles vers la base de données.
 */
public class DatabaseConnection {

    // URL de connexion JDBC :
    //   - localhost:3306 → serveur MySQL local, port par défaut
    //   - stock_fruits_legumes → nom de la base de données
    //   - useSSL=false → désactive le chiffrement SSL (inutile en local)
    //   - serverTimezone → évite une erreur de fuseau horaire
    //   - allowPublicKeyRetrieval → nécessaire avec certaines versions de MySQL 8
    private static final String URL      = "jdbc:mysql://localhost:3306/stock_fruits_legumes"
                                         + "?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";  // Utilisateur MySQL
    private static final String PASSWORD = "";       // Mot de passe MySQL (vide par défaut en local)

    // Instance unique partagée dans toute l'application (pattern Singleton)
    private static Connection instance = null;

    // Constructeur privé : on ne peut pas faire "new DatabaseConnection()" depuis l'extérieur.
    // On passe obligatoirement par getConnection().
    private DatabaseConnection() {}

    /**
     * Retourne la connexion à la base de données.
     * Si elle n'existe pas encore ou a été fermée, une nouvelle connexion est ouverte.
     */
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            // Charge manuellement le driver JDBC MySQL dans le classpath
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable. Ajoutez mysql-connector-j.jar au classpath.", e);
            }
            // Ouvre réellement la connexion vers MySQL
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instance;
    }

    /**
     * Ferme proprement la connexion à la base de données.
     * Appelée automatiquement à la fermeture de la fenêtre principale.
     */
    public static void closeConnection() {
        if (instance != null) {
            try {
                instance.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
