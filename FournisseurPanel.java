import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class FournisseurPanel extends JPanel {

    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();
    private final ArticlePanel   articlePanel;

    private final DefaultTableModel tableModel;
    private final JTable            table;

    private final JLabel lbNom     = new JLabel("-");
    private final JLabel lbTel     = new JLabel("-");
    private final JLabel lbEmail   = new JLabel("-");
    private final JLabel lbAdresse = new JLabel("-");

    private static final String[] COLONNES = {"ID", "Nom", "Telephone", "Email"};

    public FournisseurPanel(ArticlePanel articlePanel) {
        this.articlePanel = articlePanel;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLONNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFFF8E1));
                }
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) afficherFiche();
        });

        JScrollPane scroll = new JScrollPane(table);
        JPanel fiche = construireFiche();
        fiche.setPreferredSize(new Dimension(230, 0));

        JButton btnAjouter   = creerBouton("Ajouter",   new Color(0x388E3C));
        JButton btnModifier  = creerBouton("Modifier",  new Color(0x1976D2));
        JButton btnSupprimer = creerBouton("Supprimer", new Color(0xD32F2F));

        btnAjouter.addActionListener   (e -> ouvrirDialog(null));
        btnModifier.addActionListener  (e -> modifierSelectionne());
        btnSupprimer.addActionListener (e -> supprimerSelectionne());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, fiche);
        split.setResizeWeight(0.7);
        split.setDividerSize(6);

        add(split,    BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        try {
            for (Fournisseur f : fournisseurDAO.getAll()) {
                tableModel.addRow(new Object[]{
                    f.getIdFournisseur(),
                    f.getNom(),
                    f.getTelephone() != null ? f.getTelephone() : "-",
                    f.getEmail()     != null ? f.getEmail()     : "-"
                });
            }
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger les fournisseurs :\n" + ex.getMessage());
        }
        viderFiche();
    }

    private JPanel construireFiche() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        p.setBackground(new Color(0xFFFDE7));

        JLabel titre = new JLabel("Fiche fournisseur");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titre);
        p.add(Box.createVerticalStrut(12));
        p.add(ficheSection("Nom",       lbNom));
        p.add(ficheSection("Telephone", lbTel));
        p.add(ficheSection("Email",     lbEmail));
        p.add(ficheSection("Adresse",   lbAdresse));
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel ficheSection(String label, JLabel value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(0x795548));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(value);
        p.add(Box.createVerticalStrut(8));
        return p;
    }

    private void afficherFiche() {
        int row = table.getSelectedRow();
        if (row == -1) { viderFiche(); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Fournisseur f = fournisseurDAO.getById(id);
            if (f == null) { viderFiche(); return; }
            lbNom    .setText(f.getNom()      != null ? f.getNom()      : "-");
            lbTel    .setText(f.getTelephone() != null ? f.getTelephone(): "-");
            lbEmail  .setText(f.getEmail()     != null ? f.getEmail()    : "-");
            lbAdresse.setText(f.getAdresse()   != null ? f.getAdresse()  : "-");
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger la fiche :\n" + ex.getMessage());
        }
    }

    private void viderFiche() {
        lbNom.setText("-"); lbTel.setText("-");
        lbEmail.setText("-"); lbAdresse.setText("-");
    }

    private void modifierSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez un fournisseur a modifier.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            ouvrirDialog(fournisseurDAO.getById(id));
        } catch (SQLException ex) {
            afficherErreur("Erreur : " + ex.getMessage());
        }
    }

    private void supprimerSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez un fournisseur a supprimer.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nom = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer le fournisseur \"" + nom + "\" ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                if (fournisseurDAO.delete(id)) {
                    refresh();
                    articlePanel.refresh();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Impossible de supprimer : des articles sont associes a ce fournisseur.",
                        "Contrainte d'integrite", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                afficherErreur("Erreur lors de la suppression :\n" + ex.getMessage());
            }
        }
    }

    private void ouvrirDialog(Fournisseur existant) {
        boolean isModif = (existant != null);
        String  titre   = isModif ? "Modifier le fournisseur" : "Ajouter un fournisseur";

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), titre,
                                     java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        JTextField tfNom  = new JTextField(20);
        JTextField tfTel  = new JTextField(20);
        JTextField tfMail = new JTextField(20);
        JTextField tfAdr  = new JTextField(20);

        if (isModif) {
            tfNom .setText(existant.getNom()      != null ? existant.getNom()      : "");
            tfTel .setText(existant.getTelephone() != null ? existant.getTelephone(): "");
            tfMail.setText(existant.getEmail()     != null ? existant.getEmail()    : "");
            tfAdr .setText(existant.getAdresse()   != null ? existant.getAdresse()  : "");
        }

        ajouterLigne(form, gc, 0, "Nom * :",     tfNom);
        ajouterLigne(form, gc, 1, "Telephone :", tfTel);
        ajouterLigne(form, gc, 2, "Email :",     tfMail);
        ajouterLigne(form, gc, 3, "Adresse :",   tfAdr);

        JButton btnOk     = creerBouton(isModif ? "Modifier" : "Ajouter", new Color(0x388E3C));
        JButton btnCancel = new JButton("Annuler");
        btnCancel.addActionListener(e -> dialog.dispose());

        btnOk.addActionListener(e -> {
            String nom = tfNom.getText().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Le nom est obligatoire.",
                    "Saisie invalide", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Fournisseur f = new Fournisseur();
            f.setNom      (nom);
            f.setTelephone(tfTel .getText().trim());
            f.setEmail    (tfMail.getText().trim());
            f.setAdresse  (tfAdr .getText().trim());
            try {
                if (isModif) {
                    f.setIdFournisseur(existant.getIdFournisseur());
                    fournisseurDAO.update(f);
                } else {
                    fournisseurDAO.insert(f);
                }
                refresh();
                articlePanel.refresh();
                dialog.dispose();
            } catch (SQLException ex) {
                afficherErreur("Erreur base de donnees :\n" + ex.getMessage());
            }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnBar.add(btnCancel);
        btnBar.add(btnOk);

        dialog.add(form,   BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void ajouterLigne(JPanel p, GridBagConstraints gc,
                               int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.35;
        p.add(new JLabel(label), gc);
        gc.gridx = 1;              gc.weightx = 0.65;
        p.add(comp, gc);
    }

    private JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }

    private void afficherErreur(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
