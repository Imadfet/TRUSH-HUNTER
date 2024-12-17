package org.trash_hunter.trashes;

import org.trash_hunter.util.DataTransferObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Trash implements DataTransferObject {
    protected long id;                           //identifiant unique au déchet

    protected String name;                      //nom commun

    protected double x,y;                       //coordonnées
    protected int width, height;                //largeur, hauteur
    protected int nbPoints;                     //nomnbre de points rapportés
    protected BufferedImage sprite;             //image du déchet
    private int visible;                        //déchet visible ou non
    protected long creationTime;                //temps passé après création
    protected int respawnTime;             //temps de récupération en ms
    protected int appearanceRangeXInf;          //valeur inf plage d'apparition sur x
    protected int appearanceRangeXSup;          //valeur sup plage d'apparition sur x
    protected int appearanceRangeYInf;          //valeur inf plage d'apparition sur y
    protected int appearanceRangeYSup;          //valeur sup plage d'apparition sur y

    private boolean isBonus;        // Indique si le déchet est un bonus
    private long bonusDuration;     // Durée pendant laquelle le déchet bonus reste visible (en ms)
    private long duration;


    public Trash(double x,double y){
        this.x=x;
        this.y=y;
        this.appearanceRangeXInf = 0;
        this.appearanceRangeXSup = 1440-width;
        this.visible=1;
    }
    // constructeur par défault
    public Trash (){
        this(0,0);
    }
    public Trash (long id){
        this(0,0);
        this.id=id;
    }

    public Trash(double x, double y, boolean isBonus, long bonusDuration) {
        this(x, y);  // Appel au constructeur existant
        this.isBonus = isBonus;
        this.bonusDuration = bonusDuration;
    }

    public void rendering (Graphics2D contexte){
        if(this.visible==1){
            contexte.drawImage(this.sprite,(int)x,(int)y,null);
        }
    }
    public TrashDB convertTrashToTrashDB(){
        TrashDB trashDB = new TrashDB();
        trashDB.setVisible(visible);
        trashDB.setName(name);
        trashDB.setId(id);
        trashDB.setX(x);
        trashDB.setY(y);
        return(trashDB);
    }
    public void updateBonusVisibility() {
        if (isBonus) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - creationTime >= duration) {
                this.visible = 0; // Set visibility to invisible
            }
        }
    }
    /**
     * Permet de réinitialiser la position d'un déchet après qu'il ai été rendu invisible
     */
    public void updatePosition() {
        long currentTime = System.currentTimeMillis();
        if (isBonus && (currentTime - creationTime >= bonusDuration)) {
            this.setVisible(0); // Cache le déchet bonus après la durée
        } else if (!isBonus && (currentTime - creationTime >= respawnTime)) {
            // Pour les déchets normaux
            Random rdm = new Random();
            this.x = rdm.nextDouble(this.appearanceRangeXInf, this.appearanceRangeXSup + 0.5);
            this.y = rdm.nextDouble(this.appearanceRangeYInf, this.appearanceRangeYSup + 0.5);
            creationTime = currentTime;
            visible = 1;
        }
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }
    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > 1000000;
    }

    // Getters and setters


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    public int getNbPoints() {
        return nbPoints;
    }

    public boolean getIsBonus() {
        return isBonus;
    }


    public void setNbPoints(int nbPoints) {
        this.nbPoints = nbPoints;
    }
    public BufferedImage getSprite() {
        return sprite;
    }
    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public int isVisible() {
        return visible;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    public void setRespawnTime(int respawnTime) {
        this.respawnTime = respawnTime;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVisible() {
        return this.visible;
    }

    public String toString (){
        return (this.name);
    }
}
