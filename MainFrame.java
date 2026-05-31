import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Fenêtre principale de l'application.
 * Elle contient :
 *   - Un en-tête vert avec le nom de l'application
 *   - Un JTabbedPane avec deux onglets : "Articles" et "Fournisseurs"
 *   - Une barre de statut en bas indiquant la base de données utilisée
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Tonton Primeur - Gestion de stock");
        setDefaultCloseOperation(EXIT_ON_CLOSE);   // Ferme l'application quand on clique sur la croix
        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500));    // Taille minimale pour que l'UI reste lisible
        setLocationRelativeTo(null);               // Centre la fenêtre sur l'écran
        initUI();

        // Ferme proprement la connexion MySQL quand l'utilisateur ferme la fenêtre
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConnection.closeConnection();
            }
        });
    }

    /**
     * Construit et assemble tous les composants graphiques de la fenêtre.
     */
    private void initUI() {
        // --- En-tête vert en haut de la fenêtre ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        header.setBackground(new Color(0x2E7D32)); // Vert foncé Material Design

        JLabel title = new JLabel("Tonton Primeur");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Gestion de stock - Guadeloupe");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xA5D6A7)); // Vert clair

        header.add(title);
        header.add(Box.createHorizontalStrut(10)); // Espace horizontal fixe
        header.add(subtitle);

        // --- Onglets au centre : Articles et Fournisseurs ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        ArticlePanel     articlePanel     = new ArticlePanel();
        // FournisseurPanel reçoit articlePanel pour pouvoir rafraîchir la liste
        // des articles quand un fournisseur est modifié ou supprimé
        FournisseurPanel fournisseurPanel = new FournisseurPanel(articlePanel);

        tabs.addTab("Articles",     articlePanel);
        tabs.addTab("Fournisseurs", fournisseurPanel);

        // Rafraîchit la liste des articles chaque fois qu'on revient sur l'onglet Articles
        // (utile si on vient de modifier un fournisseur)
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) articlePanel.refresh();
        });

        // --- Barre de statut en bas ---
        JLabel status = new JLabel("  Connecte a stock_fruits_legumes");
        status.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        status.setForeground(Color.GRAY);
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY)); // Ligne en haut

        // Disposition générale : header en haut, onglets au centre, statut en bas
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }
}
