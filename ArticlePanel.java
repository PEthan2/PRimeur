import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Onglet "Articles" de l'application.
 *
 * Affiche la liste des articles dans un tableau et permet de :
 *   - Trier par nom, prix ou quantité (croissant/décroissant)
 *   - Ajouter un nouvel article (via un formulaire dans une fenêtre modale)
 *   - Modifier un article sélectionné
 *   - Supprimer un article sélectionné (avec confirmation)
 */
public class ArticlePanel extends JPanel {

    // DAO pour accéder aux données : articles et fournisseurs (pour le formulaire)
    private final ArticleDAO     articleDAO     = new ArticleDAO();
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();

    // Modèle de données du tableau : contient les lignes et colonnes affichées
    private final DefaultTableModel tableModel;
    private final JTable            table;

    // Colonne de tri actuelle et sens du tri (vrai = croissant)
    private String  sortCol = "nom";
    private boolean sortAsc = true;

    // Noms des colonnes affichées dans l'en-tête du tableau
    private static final String[] COLONNES = {
        "ID", "Nom", "Type", "Prix (EUR)", "Quantite", "Fournisseur"
    };

    public ArticlePanel() {
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Création du modèle de tableau avec les colonnes définies.
        // isCellEditable = false : empêche l'utilisateur de modifier directement les cellules
        tableModel = new DefaultTableModel(COLONNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Une seule ligne sélectionnable à la fois
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Cache la colonne "ID" (utile en interne mais inutile pour l'utilisateur)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

        // Alternance de couleurs entre les lignes paires (blanc) et impaires (vert très clair)
        // pour améliorer la lisibilité du tableau
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

        // --- Panneau de tri en bas à gauche ---
        JPanel triPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        triPanel.add(new JLabel("Trier par :"));

        JComboBox<String> triCombo = new JComboBox<>(
            new String[]{"Nom", "Prix unitaire", "Quantite"});
        triCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Bouton bascule : "Croissant" ou "Décroissant"
        JToggleButton triDir = new JToggleButton("Croissant");
        triDir.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Quand on change la colonne de tri → met à jour sortCol et recharge les données
        triCombo.addActionListener(e -> {
            sortCol = switch (triCombo.getSelectedIndex()) {
                case 1  -> "prix_unitaire";
                case 2  -> "quantite_stock";
                default -> "nom";
            };
            refresh();
        });
        // Quand on clique sur le bouton de direction → inverse sortAsc et recharge
        triDir.addActionListener(e -> {
            sortAsc = !triDir.isSelected();
            triDir.setText(sortAsc ? "Croissant" : "Decroissant");
            refresh();
        });

        triPanel.add(triCombo);
        triPanel.add(triDir);

        // --- Boutons d'action en bas à droite ---
        JButton btnAjouter   = creerBouton("Ajouter",   new Color(0x388E3C)); // Vert
        JButton btnModifier  = creerBouton("Modifier",  new Color(0x1976D2)); // Bleu
        JButton btnSupprimer = creerBouton("Supprimer", new Color(0xD32F2F)); // Rouge

        // null = pas d'article existant → mode création
        btnAjouter.addActionListener   (e -> ouvrirDialogArticle(null));
        btnModifier.addActionListener  (e -> modifierSelectionne());
        btnSupprimer.addActionListener (e -> supprimerSelectionne());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        // Barre du bas : tri à gauche, boutons à droite
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(triPanel, BorderLayout.WEST);
        bottom.add(btnPanel, BorderLayout.EAST);

        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // Chargement initial des données au démarrage
        refresh();
    }

    /**
     * Recharge la liste des articles depuis la base de données
     * en appliquant le tri courant.
     * Appelée au démarrage et après chaque modification (ajout, modif, suppression).
     */
    public void refresh() {
        tableModel.setRowCount(0); // Vide le tableau avant de le remplir
        try {
            List<Article> articles = articleDAO.getAll(sortCol, sortAsc);
            for (Article a : articles) {
                tableModel.addRow(new Object[]{
                    a.getIdArticle(),
                    a.getNom(),
                    a.getType(),
                    String.format("%.2f", a.getPrixUnitaire()), // Formatage à 2 décimales
                    a.getQuantiteStock(),
                    // Affiche "-" si l'article n'a pas de fournisseur assigné
                    a.getNomFournisseur() != null ? a.getNomFournisseur() : "-"
                });
            }
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger les articles :\n" + ex.getMessage());
        }
    }

    /**
     * Récupère l'article sélectionné dans le tableau et ouvre le formulaire de modification.
     * Affiche un avertissement si aucune ligne n'est sélectionnée.
     */
    private void modifierSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez un article a modifier.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Recrée un objet Article depuis les données affichées dans le tableau
        Article a = new Article();
        a.setIdArticle    ((int)    tableModel.getValueAt(row, 0));
        a.setNom          ((String) tableModel.getValueAt(row, 1));
        a.setType         ((String) tableModel.getValueAt(row, 2));
        // Le prix est affiché en String formaté → on le reparse en double (gestion virgule/point)
        a.setPrixUnitaire (Double.parseDouble(((String) tableModel.getValueAt(row, 3)).replace(",", ".")));
        a.setQuantiteStock((int)    tableModel.getValueAt(row, 4));
        ouvrirDialogArticle(a); // Ouvre le formulaire en mode modification
    }

    /**
     * Demande confirmation puis supprime l'article sélectionné.
     */
    private void supprimerSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez un article a supprimer.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nom = (String) tableModel.getValueAt(row, 1);
        // Boîte de dialogue de confirmation avant suppression définitive
        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer l'article \"" + nom + "\" ?",
            "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            try {
                articleDAO.delete(id);
                refresh(); // Rafraîchit le tableau après suppression
            } catch (SQLException ex) {
                afficherErreur("Erreur lors de la suppression :\n" + ex.getMessage());
            }
        }
    }

    /**
     * Ouvre une fenêtre modale (formulaire) pour ajouter ou modifier un article.
     *
     * @param articleExistant null → mode ajout, sinon → mode modification pré-rempli
     */
    private void ouvrirDialogArticle(Article articleExistant) {
        boolean isModif = (articleExistant != null);
        String titre    = isModif ? "Modifier l'article" : "Ajouter un article";

        // APPLICATION_MODAL = bloque l'interaction avec la fenêtre principale tant que le dialog est ouvert
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), titre,
                                     java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        // GridBagLayout : grille flexible, utilisée ici pour aligner labels et champs en 2 colonnes
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 4, 4, 4);
        gc.fill    = GridBagConstraints.HORIZONTAL; // Les champs s'étirent horizontalement

        // Champs de saisie du formulaire
        JTextField        tfNom  = new JTextField(20);
        JComboBox<String> cbType = new JComboBox<>(new String[]{"fruit", "legume"});
        JTextField        tfPrix = new JTextField(10);
        JTextField        tfQte  = new JTextField(10);
        JComboBox<Fournisseur> cbFourn = new JComboBox<>();

        // Remplit la liste déroulante des fournisseurs depuis la BDD
        try {
            for (Fournisseur f : fournisseurDAO.getAll()) cbFourn.addItem(f);
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger les fournisseurs.");
        }

        // En mode modification : pré-remplit les champs avec les valeurs existantes
        if (isModif) {
            tfNom.setText(articleExistant.getNom());
            cbType.setSelectedItem(articleExistant.getType());
            tfPrix.setText(String.valueOf(articleExistant.getPrixUnitaire()));
            tfQte.setText (String.valueOf(articleExistant.getQuantiteStock()));
            // Sélectionne le bon fournisseur dans la liste déroulante
            for (int i = 0; i < cbFourn.getItemCount(); i++) {
                if (cbFourn.getItemAt(i).getIdFournisseur() == articleExistant.getIdFournisseur()) {
                    cbFourn.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Ajoute chaque champ avec son label dans le formulaire
        ajouterLigne(form, gc, 0, "Nom :", tfNom);
        ajouterLigne(form, gc, 1, "Type :", cbType);
        ajouterLigne(form, gc, 2, "Prix (EUR) :", tfPrix);
        ajouterLigne(form, gc, 3, "Quantite :", tfQte);
        ajouterLigne(form, gc, 4, "Fournisseur :", cbFourn);

        JButton btnOk     = creerBouton(isModif ? "Modifier" : "Ajouter", new Color(0x388E3C));
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.addActionListener(e -> dialog.dispose()); // Ferme le dialog sans rien faire

        // Action du bouton OK : valide les données et sauvegarde en BDD
        btnOk.addActionListener(e -> {
            try {
                String nom = tfNom.getText().trim();
                if (nom.isEmpty()) throw new IllegalArgumentException("Le nom est obligatoire.");
                // Remplace la virgule par un point pour accepter les deux formats décimaux
                double prix = Double.parseDouble(tfPrix.getText().trim().replace(",", "."));
                int    qte  = Integer.parseInt  (tfQte.getText().trim());
                Fournisseur fourn = (Fournisseur) cbFourn.getSelectedItem();
                if (fourn == null) throw new IllegalArgumentException("Selectionnez un fournisseur.");

                // Construit l'objet Article avec les valeurs saisies
                Article a = new Article();
                a.setNom          (nom);
                a.setType         ((String) cbType.getSelectedItem());
                a.setPrixUnitaire (prix);
                a.setQuantiteStock(qte);
                a.setIdFournisseur(fourn.getIdFournisseur());

                if (isModif) {
                    a.setIdArticle(articleExistant.getIdArticle()); // Conserve l'ID pour le UPDATE
                    articleDAO.update(a);
                } else {
                    articleDAO.insert(a);
                }
                refresh();          // Actualise le tableau
                dialog.dispose();   // Ferme le formulaire
            } catch (NumberFormatException ex) {
                // L'utilisateur a saisi une valeur non numérique dans prix ou quantité
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
        dialog.setVisible(true); // Affiche et bloque (modal) jusqu'à fermeture
    }

    /**
     * Ajoute une ligne label + composant dans un formulaire GridBagLayout.
     *
     * @param p     Le panneau cible
     * @param gc    Les contraintes GridBag (réutilisées et modifiées)
     * @param row   Numéro de ligne dans la grille
     * @param label Texte du label
     * @param comp  Composant de saisie (JTextField, JComboBox, etc.)
     */
    private void ajouterLigne(JPanel p, GridBagConstraints gc,
                               int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.3; // Colonne 0 : label (30% de la largeur)
        p.add(new JLabel(label), gc);
        gc.gridx = 1;              gc.weightx = 0.7; // Colonne 1 : champ de saisie (70%)
        p.add(comp, gc);
    }

    /**
     * Crée un bouton coloré avec texte blanc, style flat (sans bordure).
     */
    private JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);  // Supprime le contour de focus (carré en pointillé)
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false); // Supprime la bordure par défaut du bouton
        btn.setOpaque(true);         // Nécessaire sur certains OS pour afficher la couleur de fond
        return btn;
    }

    /**
     * Affiche une boîte de dialogue d'erreur avec le message donné.
     */
    private void afficherErreur(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
