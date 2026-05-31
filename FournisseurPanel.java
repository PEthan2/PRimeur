import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Onglet "Fournisseurs" de l'application.
 *
 * Affiche la liste des fournisseurs dans un tableau à gauche
 * et une "fiche détail" à droite qui se met à jour quand on sélectionne une ligne.
 *
 * Permet de :
 *   - Ajouter un nouveau fournisseur
 *   - Modifier un fournisseur sélectionné
 *   - Supprimer un fournisseur (refusé si des articles lui sont liés)
 *
 * Reçoit une référence à ArticlePanel pour rafraîchir la liste des articles
 * quand un fournisseur est modifié ou supprimé.
 */
public class FournisseurPanel extends JPanel {

    // DAO pour accéder aux fournisseurs en base de données
    private final FournisseurDAO fournisseurDAO = new FournisseurDAO();
    // Référence vers l'onglet Articles pour le rafraîchir si nécessaire
    private final ArticlePanel   articlePanel;

    private final DefaultTableModel tableModel;
    private final JTable            table;

    // Labels de la fiche détail à droite (mis à jour à chaque sélection dans le tableau)
    private final JLabel lbNom     = new JLabel("-");
    private final JLabel lbTel     = new JLabel("-");
    private final JLabel lbEmail   = new JLabel("-");
    private final JLabel lbAdresse = new JLabel("-");

    // Colonnes affichées dans le tableau (la colonne ID est masquée visuellement)
    private static final String[] COLONNES = {"ID", "Nom", "Telephone", "Email"};

    public FournisseurPanel(ArticlePanel articlePanel) {
        this.articlePanel = articlePanel;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Modèle de tableau non éditable directement par l'utilisateur
        tableModel = new DefaultTableModel(COLONNES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Cache la colonne ID (index 0) visuellement : min/max/width = 0
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Alternance de couleurs entre les lignes : blanc et jaune très clair
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

        // Met à jour la fiche détail à droite quand une ligne est sélectionnée.
        // getValueIsAdjusting() vaut true pendant que la sélection change encore
        // (ex: glissement de souris) → on attend qu'elle soit stabilisée.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) afficherFiche();
        });

        JScrollPane scroll = new JScrollPane(table);

        // Construit le panneau de fiche à droite et lui donne une largeur fixe
        JPanel fiche = construireFiche();
        fiche.setPreferredSize(new Dimension(230, 0));

        // --- Boutons d'action ---
        JButton btnAjouter   = creerBouton("Ajouter",   new Color(0x388E3C));
        JButton btnModifier  = creerBouton("Modifier",  new Color(0x1976D2));
        JButton btnSupprimer = creerBouton("Supprimer", new Color(0xD32F2F));

        btnAjouter.addActionListener   (e -> ouvrirDialog(null));       // null = mode création
        btnModifier.addActionListener  (e -> modifierSelectionne());
        btnSupprimer.addActionListener (e -> supprimerSelectionne());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        // JSplitPane : divise l'espace en deux (tableau à gauche, fiche à droite).
        // resizeWeight(0.7) = le tableau prend 70% de l'espace au départ.
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, fiche);
        split.setResizeWeight(0.7);
        split.setDividerSize(6);

        add(split,    BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        refresh(); // Chargement initial
    }

    /**
     * Recharge la liste des fournisseurs depuis la BDD et vide la fiche détail.
     */
    public void refresh() {
        tableModel.setRowCount(0); // Efface toutes les lignes du tableau
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
        viderFiche(); // Réinitialise la fiche détail
    }

    /**
     * Construit le panneau de fiche détail affiché à droite du tableau.
     * Ce panneau affiche toutes les informations du fournisseur sélectionné.
     */
    private JPanel construireFiche() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); // Empile les éléments verticalement
        // Bordure composée : ligne grise à gauche + marge intérieure
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        p.setBackground(new Color(0xFFFDE7)); // Fond jaune très clair

        JLabel titre = new JLabel("Fiche fournisseur");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titre);
        p.add(Box.createVerticalStrut(12)); // Espace vertical fixe
        // Ajoute chaque section de la fiche (label + valeur)
        p.add(ficheSection("Nom",       lbNom));
        p.add(ficheSection("Telephone", lbTel));
        p.add(ficheSection("Email",     lbEmail));
        p.add(ficheSection("Adresse",   lbAdresse));
        p.add(Box.createVerticalGlue()); // Pousse le contenu vers le haut
        return p;
    }

    /**
     * Crée un petit bloc "titre + valeur" pour afficher un champ dans la fiche détail.
     *
     * @param label Nom du champ (ex: "Nom", "Email")
     * @param value Label dynamique dont le texte sera mis à jour lors de la sélection
     */
    private JPanel ficheSection(String label, JLabel value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false); // Transparent : laisse voir le fond jaune du parent
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(new Color(0x795548)); // Marron (Material Design Brown)
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(value);
        p.add(Box.createVerticalStrut(8));
        return p;
    }

    /**
     * Met à jour la fiche détail avec les informations du fournisseur sélectionné.
     * Fait une requête BDD pour avoir toutes les infos (y compris l'adresse,
     * qui n'est pas dans le tableau).
     */
    private void afficherFiche() {
        int row = table.getSelectedRow();
        if (row == -1) { viderFiche(); return; } // Aucune ligne sélectionnée
        int id = (int) tableModel.getValueAt(row, 0); // Récupère l'ID caché
        try {
            Fournisseur f = fournisseurDAO.getById(id); // Requête BDD complète
            if (f == null) { viderFiche(); return; }
            lbNom    .setText(f.getNom()      != null ? f.getNom()      : "-");
            lbTel    .setText(f.getTelephone() != null ? f.getTelephone(): "-");
            lbEmail  .setText(f.getEmail()     != null ? f.getEmail()    : "-");
            lbAdresse.setText(f.getAdresse()   != null ? f.getAdresse()  : "-");
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger la fiche :\n" + ex.getMessage());
        }
    }

    /**
     * Remet tous les champs de la fiche détail à "-" (état vide).
     */
    private void viderFiche() {
        lbNom.setText("-"); lbTel.setText("-");
        lbEmail.setText("-"); lbAdresse.setText("-");
    }

    /**
     * Récupère le fournisseur sélectionné et ouvre le formulaire de modification.
     */
    private void modifierSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez un fournisseur a modifier.",
                "Aucune selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0); // ID masqué en colonne 0
        try {
            ouvrirDialog(fournisseurDAO.getById(id)); // Ouvre le formulaire pré-rempli
        } catch (SQLException ex) {
            afficherErreur("Erreur : " + ex.getMessage());
        }
    }

    /**
     * Demande confirmation puis tente de supprimer le fournisseur sélectionné.
     * La suppression est refusée si des articles sont encore associés à ce fournisseur.
     */
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
                    // Suppression réussie : rafraîchit les deux onglets
                    refresh();
                    articlePanel.refresh(); // Les articles n'ont plus ce fournisseur
                } else {
                    // FournisseurDAO.delete() a retourné false : des articles sont liés
                    JOptionPane.showMessageDialog(this,
                        "Impossible de supprimer : des articles sont associes a ce fournisseur.",
                        "Contrainte d'integrite", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                afficherErreur("Erreur lors de la suppression :\n" + ex.getMessage());
            }
        }
    }

    /**
     * Ouvre une fenêtre modale pour ajouter ou modifier un fournisseur.
     *
     * @param existant null → mode ajout, sinon → mode modification (formulaire pré-rempli)
     */
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

        // Champs de saisie du formulaire
        JTextField tfNom  = new JTextField(20);
        JTextField tfTel  = new JTextField(20);
        JTextField tfMail = new JTextField(20);
        JTextField tfAdr  = new JTextField(20);

        // En mode modification : pré-remplit les champs avec les valeurs existantes
        // Le "!= null ? ... : """" évite d'afficher "null" dans les champs vides
        if (isModif) {
            tfNom .setText(existant.getNom()      != null ? existant.getNom()      : "");
            tfTel .setText(existant.getTelephone() != null ? existant.getTelephone(): "");
            tfMail.setText(existant.getEmail()     != null ? existant.getEmail()    : "");
            tfAdr .setText(existant.getAdresse()   != null ? existant.getAdresse()  : "");
        }

        ajouterLigne(form, gc, 0, "Nom * :",     tfNom);   // * indique un champ obligatoire
        ajouterLigne(form, gc, 1, "Telephone :", tfTel);
        ajouterLigne(form, gc, 2, "Email :",     tfMail);
        ajouterLigne(form, gc, 3, "Adresse :",   tfAdr);

        JButton btnOk     = creerBouton(isModif ? "Modifier" : "Ajouter", new Color(0x388E3C));
        JButton btnCancel = new JButton("Annuler");
        btnCancel.addActionListener(e -> dialog.dispose());

        // Action du bouton OK : vérifie le nom puis sauvegarde en BDD
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
                    f.setIdFournisseur(existant.getIdFournisseur()); // Conserve l'ID pour le UPDATE
                    fournisseurDAO.update(f);
                } else {
                    fournisseurDAO.insert(f);
                }
                refresh();              // Actualise la liste des fournisseurs
                articlePanel.refresh(); // Les articles peuvent afficher un nouveau nom de fournisseur
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

    /**
     * Ajoute une ligne label + composant dans un formulaire GridBagLayout.
     */
    private void ajouterLigne(JPanel p, GridBagConstraints gc,
                               int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.35; // Label : 35% de la largeur
        p.add(new JLabel(label), gc);
        gc.gridx = 1;              gc.weightx = 0.65; // Champ : 65%
        p.add(comp, gc);
    }

    /**
     * Crée un bouton coloré avec texte blanc, style flat.
     */
    private JButton creerBouton(String texte, Color couleur) {
        JButton btn = new JButton(texte);
        btn.setBackground(couleur);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorderPainted(false);
        btn.setOpaque(true); // Nécessaire sur certains OS pour afficher la couleur de fond
        return btn;
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     */
    private void afficherErreur(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
