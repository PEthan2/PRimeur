import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Tonton Primeur - Gestion de stock");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        initUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseConnection.closeConnection();
            }
        });
    }

    private void initUI() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        header.setBackground(new Color(0x2E7D32));

        JLabel title = new JLabel("Tonton Primeur");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Gestion de stock - Guadeloupe");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xA5D6A7));

        header.add(title);
        header.add(Box.createHorizontalStrut(10));
        header.add(subtitle);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        ArticlePanel     articlePanel     = new ArticlePanel();
        FournisseurPanel fournisseurPanel = new FournisseurPanel(articlePanel);

        tabs.addTab("Articles",     articlePanel);
        tabs.addTab("Fournisseurs", fournisseurPanel);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) articlePanel.refresh();
        });

        JLabel status = new JLabel("  Connecte a stock_fruits_legumes");
        status.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        status.setForeground(Color.GRAY);
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }
}
