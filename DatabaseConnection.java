import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/stock_fruits_legumes"
                                         + "?useSSL=false&serverTimezone=Europe/Paris&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection instance = null;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL introuvable. Ajoutez mysql-connector-j.jar au classpath.", e);
            }
            instance = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return instance;
    }

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
