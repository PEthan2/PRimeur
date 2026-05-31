/**
 * Modèle (entité) représentant un fournisseur.
 *
 * Correspond à la table "fournisseur" en base de données :
 *   id_fournisseur | nom | telephone | email | adresse
 *
 * C'est un simple POJO (Plain Old Java Object) : stocke des données
 * et expose des getters/setters. Pas de logique métier ici.
 */
public class Fournisseur {

    private int    idFournisseur; // Clé primaire (AUTO_INCREMENT en BDD)
    private String nom;           // Nom du fournisseur (obligatoire)
    private String telephone;     // Numéro de téléphone (optionnel)
    private String email;         // Adresse e-mail (optionnel)
    private String adresse;       // Adresse postale (optionnel)

    // Constructeur vide : permet de créer un Fournisseur vide puis de remplir ses champs
    public Fournisseur() {}

    // Constructeur complet : utilisé dans FournisseurDAO.map() pour créer directement
    // un Fournisseur à partir d'une ligne de résultat SQL (ResultSet)
    public Fournisseur(int idFournisseur, String nom, String telephone,
                       String email, String adresse) {
        this.idFournisseur = idFournisseur;
        this.nom           = nom;
        this.telephone     = telephone;
        this.email         = email;
        this.adresse       = adresse;
    }

    // --- Getters et Setters ---
    // Permettent d'accéder aux champs privés depuis les autres classes (encapsulation)

    public int    getIdFournisseur()                     { return idFournisseur; }
    public void   setIdFournisseur(int id)               { this.idFournisseur = id; }
    public String getNom()                               { return nom; }
    public void   setNom(String nom)                     { this.nom = nom; }
    public String getTelephone()                         { return telephone; }
    public void   setTelephone(String telephone)         { this.telephone = telephone; }
    public String getEmail()                             { return email; }
    public void   setEmail(String email)                 { this.email = email; }
    public String getAdresse()                           { return adresse; }
    public void   setAdresse(String adresse)             { this.adresse = adresse; }

    // Utilisé par JComboBox pour afficher le nom du fournisseur dans les listes déroulantes
    @Override
    public String toString() { return nom; }
}
