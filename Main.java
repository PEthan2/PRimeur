import javax.swing.*;

/**
 * Point d'entrée de l'application "Tonton Primeur".
 * C'est ici que le programme démarre (méthode main).
 */
public class Main {
    public static void main(String[] args) {
        // Applique le style graphique natif du système d'exploitation (Windows, Mac, Linux)
        // pour que la fenêtre ressemble aux applications habituelles de l'OS.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // SwingUtilities.invokeLater garantit que la fenêtre est créée
        // dans le thread dédié à l'interface graphique (EDT - Event Dispatch Thread).
        // C'est obligatoire en Swing pour éviter les problèmes de concurrence.
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(); // Crée la fenêtre principale
            frame.setVisible(true);            // Rend la fenêtre visible à l'écran
        });
    }
}
