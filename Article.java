/**
 * Modèle (entité) représentant un article du stock.
 *
 * Correspond à la table "article" en base de données :
 *   id_article | nom | type | prix_unitaire | quantite_stock | id_fournisseur
 *
 * C'est un simple POJO (Plain Old Java Object) : il stocke des données
 * et expose des getters/setters pour y accéder. Pas de logique métier ici.
 */
public class Article {

    private int     idArticle;      // Clé primaire (AUTO_INCREMENT en BDD)
    private String  nom;            // Nom du produit (ex: "Banane")
    private String  type;           // Catégorie : "fruit" ou "legume"
    private double  prixUnitaire;   // Prix à l'unité en euros
    private int     quantiteStock;  // Quantité disponible en stock
    private int     idFournisseur;  // Clé étrangère vers la table fournisseur
    private String  nomFournisseur; // Nom du fournisseur (non stocké en BDD, rempli par JOIN SQL)

    // Constructeur vide nécessaire pour créer un Article puis remplir ses champs un par un
    public Article() {}

    // Constructeur complet (sans nomFournisseur car c'est un champ calculé par JOIN)
    public Article(int idArticle, String nom, String type,
                   double prixUnitaire, int quantiteStock, int idFournisseur) {
        this.idArticle     = idArticle;
        this.nom           = nom;
        this.type          = type;
        this.prixUnitaire  = prixUnitaire;
        this.quantiteStock = quantiteStock;
        this.idFournisseur = idFournisseur;
    }

    // --- Getters et Setters ---
    // Permettent d'accéder aux champs privés depuis les autres classes (encapsulation)

    public int    getIdArticle()                          { return idArticle; }
    public void   setIdArticle(int id)                   { this.idArticle = id; }
    public String getNom()                               { return nom; }
    public void   setNom(String nom)                     { this.nom = nom; }
    public String getType()                              { return type; }
    public void   setType(String type)                   { this.type = type; }
    public double getPrixUnitaire()                      { return prixUnitaire; }
    public void   setPrixUnitaire(double prix)           { this.prixUnitaire = prix; }
    public int    getQuantiteStock()                     { return quantiteStock; }
    public void   setQuantiteStock(int quantite)         { this.quantiteStock = quantite; }
    public int    getIdFournisseur()                     { return idFournisseur; }
    public void   setIdFournisseur(int idFournisseur)    { this.idFournisseur = idFournisseur; }
    public String getNomFournisseur()                    { return nomFournisseur; }
    public void   setNomFournisseur(String nomFournisseur) { this.nomFournisseur = nomFournisseur; }

    // Utilisé par JComboBox pour afficher le nom de l'article dans les listes déroulantes
    @Override
    public String toString() { return nom; }
}
