import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ArticlePanel extends JPanel {

    private final ArticleDAO     articleDAO     = new ArticleDAO();
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();

    private final DefaultTableModel tableModel;
    private final JTable            table;

    private String  sortCol = "nom";
    private boolean sortAsc = true;

    private static final String[] COLONNES = {
        "ID", "Nom", "Type", "Prix (EUR)", "Quantite", "Fournisseur"
    };

    public ArticlePanel() {
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLONNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF1F8E9));
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);

        JPanel triPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        triPanel.add(new JLabel("Trier par :"));

        JComboBox<String> triCombo = new JComboBox<>(
            new String[]{"Nom", "Prix unitaire", "Quantite"});
        triCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JToggleButton triDir = new JToggleButton("Croissant");
        triDir.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        triCombo.addActionListener(e -> {
            sortCol = switch (triCombo.getSelectedIndex()) {
                case 1  -> "prix_unitaire";
                case 2  -> "quantite_stock";
                default -> "nom";
            };
            refresh();
        });
        triDir.addActionListener(e -> {
            sortAsc = !triDir.isSelected();
            triDir.setText(sortAsc ? "Croissant" : "Decroissant");
            refresh();
        });

        triPanel.add(triCombo);
        triPanel.add(triDir);

        JButton btnAjouter   = creerBouton("Ajouter",   new Color(0x388E3C));
        JButton btnModifier  = creerBouton("Modifier",  new Color(0x1976D2));
        JButton btnSupprimer = creerBouton("Supprimer", new Color(0xD32F2F));

        btnAjouter.addActionListener   (e -> ouvrirDialogArticle(null));
        btnModifier.addActionListener  (e -> modifierSelectionne());
        btnSupprimer.addActionListener (e -> supprimerSelectionne());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(triPanel, BorderLayout.WEST);
        bottom.add(btnPanel, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        try {
            List<Article> articles = articleDAO.getAll(sortCol, sortAsc);
            for (Article a : articles) {
                tableModel.addRow(new Object[]{
                    a.getIdArticle(),
                    a.getNom(),
                    a.getType(),
                    String.format("%.2f", a.getPrixUnitaire()),
                    a.getQuantiteStock(),
                    a.getNomFournisseur() != null ? a.getNomFournisseur() : "-"
                });
            }
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger les articles :\n" + ex.getMessage());
        }
    }

    private void modifierSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez un article a modifier.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Article a = new Article();
        a.setIdArticle    ((int)    tableModel.getValueAt(row, 0));
        a.setNom          ((String) tableModel.getValueAt(row, 1));
        a.setType         ((String) tableModel.getValueAt(row, 2));
        a.setPrixUnitaire (Double.parseDouble(((String) tableModel.getValueAt(row, 3)).replace(",", ".")));
        a.setQuantiteStock((int)    tableModel.getValueAt(row, 4));
        ouvrirDialogArticle(a);
    }

    private void supprimerSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez un article a supprimer.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nom = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer l'article \"" + nom + "\" ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                articleDAO.delete(id);
                refresh();
            } catch (SQLException ex) {
                afficherErreur("Erreur lors de la suppression :\n" + ex.getMessage());
            }
        }
    }

    private void ouvrirDialogArticle(Article articleExistant) {
        boolean isModif = (articleExistant != null);
        String titre    = isModif ? "Modifier l'article" : "Ajouter un article";

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), titre,
                                     java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 4, 4, 4);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        JTextField        tfNom  = new JTextField(20);
        JComboBox<String> cbType = new JComboBox<>(new String[]{"fruit", "legume"});
        JTextField        tfPrix = new JTextField(10);
        JTextField        tfQte  = new JTextField(10);
        JComboBox<Fournisseur> cbFourn = new JComboBox<>();

        try {
            for (Fournisseur f : fournisseurDAO.getAll()) cbFourn.addItem(f);
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger les fournisseurs.");
        }

        if (isModif) {
            tfNom.setText(articleExistant.getNom());
            cbType.setSelectedItem(articleExistant.getType());
            tfPrix.setText(String.valueOf(articleExistant.getPrixUnitaire()));
            tfQte.setText (String.valueOf(articleExistant.getQuantiteStock()));
            for (int i = 0; i < cbFourn.getItemCount(); i++) {
                if (cbFourn.getItemAt(i).getIdFournisseur() == articleExistant.getIdFournisseur()) {
                    cbFourn.setSelectedIndex(i);
                    break;
                }
            }
        }

        ajouterLigne(form, gc, 0, "Nom :", tfNom);
        ajouterLigne(form, gc, 1, "Type :", cbType);
        ajouterLigne(form, gc, 2, "Prix (EUR) :", tfPrix);
        ajouterLigne(form, gc, 3, "Quantite :", tfQte);
        ajouterLigne(form, gc, 4, "Fournisseur :", cbFourn);

        JButton btnOk     = creerBouton(isModif ? "Modifier" : "Ajouter", new Color(0x388E3C));
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose());

        btnOk.addActionListener(e -> {
            try {
                String nom = tfNom.getText().trim();
                if (nom.isEmpty()) throw new IllegalArgumentException("Le nom est obligatoire.");
                double prix = Double.parseDouble(tfPrix.getText().trim().replace(",", "."));
                int    qte  = Integer.parseInt  (tfQte.getText().trim());
                Fournisseur fourn = (Fournisseur) cbFourn.getSelectedItem();
                if (fourn == null) throw new IllegalArgumentException("Selectionnez un fournisseur.");

                Article a = new Article();
                a.setNom          (nom);
                a.setType         ((String) cbType.getSelectedItem());
                a.setPrixUnitaire (prix);
                a.setQuantiteStock(qte);
                a.setIdFournisseur(fourn.getIdFournisseur());

                if (isModif) {
                    a.setIdArticle(articleExistant.getIdArticle());
                    articleDAO.update(a);
                } else {
                    articleDAO.insert(a);
                }
                refresh();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Le prix et la quantite doivent etre des nombres valides.",
                    "Saisie invalide", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                    "Saisie invalide", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                afficherErreur("Erreur base de donnees :\n" + ex.getMessage());
            }
        });

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnBar.add(btnAnnuler);
        btnBar.add(btnOk);

        dialog.add(form,   BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void ajouterLigne(JPanel p, GridBagConstraints gc,
                               int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.3;
        p.add(new JLabel(label), gc);
        gc.gridx = 1;              gc.weightx = 0.7;
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
