public class Article {

    private int     idArticle;
    private String  nom;
    private String  type;
    private double  prixUnitaire;
    private int     quantiteStock;
    private int     idFournisseur;
    private String  nomFournisseur;

    public Article() {}

    public Article(int idArticle, String nom, String type,
                   double prixUnitaire, int quantiteStock, int idFournisseur) {
        this.idArticle     = idArticle;
        this.nom           = nom;
        this.type          = type;
        this.prixUnitaire  = prixUnitaire;
        this.quantiteStock = quantiteStock;
        this.idFournisseur = idFournisseur;
    }

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

    @Override
    public String toString() { return nom; }
}
