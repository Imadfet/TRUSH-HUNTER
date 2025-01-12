package org.trash_hunter;

import org.trash_hunter.trashes.*;
import org.trash_hunter.user.Avatar;
import org.trash_hunter.user.Diver;
import org.trash_hunter.user.DiverDAO;
import org.trash_hunter.util.DatabaseConnection;
import org.trash_hunter.util.SoundManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {
    private BufferedImage backgroundImage;                              // Image de fond du jeu
    private final Avatar myAvatar;                                      // Avatar du joueur
    private Diver diver;                                                // Plongeur actuel
    private DiverDAO diverDAO;                                          // DAO pour les plongeurs
    private List<Diver> allDivers;                                      // Liste de tous les plongeurs
    private TrashDAO trashDAO;                                          // DAO pour les déchets
    private List<Trash> localTrashset;                                  // Liste des déchets locaux
    private final Random randomNbr;                                     // Générateur de nombres aléatoires
    private static final int NB_TRASHES = 30;                           // Nombre de déchets à initialiser
    private long lastBonusSpawnTime = 0; // Track time of the last bonus spawn
    private final long BONUS_SPAWN_INTERVAL = 10000; // Interval in milliseconds (10 seconds)


    public Game(String pseudo, String color) throws SQLException{
        this.randomNbr = new Random();
        loadBackgroundImage();                                              // Chargement de l'image de fond
        this.diver = new Diver(pseudo, color);                              // Création du plongeur
        this.diverDAO = new DiverDAO(DatabaseConnection.getConnection());   // Connection à la table Diver
        this.diverDAO.create(diver);                                        // Enregistrement du plongeur dans la base de données
        this.myAvatar = diver.convertDiverToAvatar();                       // Conversion du plongeur en avatar

        // Initialisation des déchets si c'est le premier joueur
        this.trashDAO = new TrashDAO(DatabaseConnection.getConnection());
        if (trashDAO.findAll().isEmpty()) {
            initTrashes();                                                  // Initialise les déchets pour le premier joueur
        }
        initLocalTrashes();                                                 // Initialise les déchets locaux
    }

    public Game() throws SQLException{
        this("Bob", "Blue");
    }

    private void loadBackgroundImage() {
        try {
            this.backgroundImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("fond_marin_1440x780.png")));
        } catch (IOException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Méthode de rendu
    public void rendering(Graphics2D contexte) {
        contexte.drawImage(this.backgroundImage, 0, 0, null);
        contexte.drawString("Score : " + this.myAvatar.getScore(), 10, 20);     // Affiche le score

        // Rendu des avatars des autres plongeurs
        for (Diver otherDivers : allDivers) {
            Avatar avatar = otherDivers.convertDiverToAvatar();
            avatar.rendering(contexte);
        }

        // Rendu des déchets locaux
        for (Trash trash : this.localTrashset) {
            trash.rendering(contexte);
        }
    }

    // Méthode de mise à jour du jeu
    public void update() {
        // Spawn bonus trash every 10 seconds
        if (System.currentTimeMillis() - lastBonusSpawnTime > BONUS_SPAWN_INTERVAL) {
            spawnBonusTrash();
            lastBonusSpawnTime = System.currentTimeMillis();
        }

        // Update logic for all trashes
        for (Trash trash : localTrashset) {
            trash.updateBonusVisibility(); // Ensure bonus trash visibility is updated
        }

        // Other update logic (like collision checks, avatar movement, etc.)
        updateAfterCollisionDiverTrash();
    }



    // Méthode pour vérifier si le jeu est terminé
    public boolean isFinished() {
        return false;
        // Le jeu n'a pas de fin
    }


    // Mise à jour après collision entre le plongeur et les déchets
    public void updateAfterCollisionDiverTrash() {
        for (int id = 0; id < localTrashset.size(); id++) {
            Trash trash = localTrashset.get(id);

            // Check collision and visibility
            if (isColliding(trash, myAvatar) && trash.getVisible() == 1) {
                myAvatar.setScore(myAvatar.getScore() + trash.getNbPoints()); // Add points
                myAvatar.updateScoreHistory(); // Update score history

                trash.setVisible(0); // Hide collected trash
                if (trash.getIsBonus()) {
                    // Play special sound for bonus trash
                    SoundManager.playSound("resources/sounds/bonus_collect.wav");
                } else {
                    // Play normal trash collection sound
                    SoundManager.playSound("resources/sounds/collect_trash.wav");
                }

                // Update trash in the database
                trashDAO.update(trash.convertTrashToTrashDB(), id);
            }
        }
    }



    // Mise à jour des déchets locaux en fonction des données de la base
    public void updateLocalTrashes() {
        List<TrashDB> dataBaseTrashset = trashDAO.findAll();    // Récupération des déchets de la base de données

        // Mise à jour des coordonnées des déchets locaux
        for (TrashDB trash : dataBaseTrashset) {
            long id = trash.getId();
            double x = trash.getX();
            double y = trash.getY();
            int visible = trash.getVisible();

            // Vérification si la position a changé
            if (!(x == localTrashset.get((int) id).getX()) ||
                    !(y == localTrashset.get((int) id).getY()) ||
                    !(visible == localTrashset.get((int)id).getVisible())) {

                Trash trashCopy = localTrashset.get((int) id);
                trashCopy.setX(x); // Mise à jour de la position X
                trashCopy.setY(y); // Mise à jour de la position Y
                trashCopy.setVisible(visible); // Mise à jour de la visibilité
                localTrashset.set((int) id, trashCopy); // Remplacement de l'ancien déchet par le nouveau
            }
        }
    }
    private void spawnBonusTrash() {
        // Generate random position
        double x = Math.random() * 800; // Adjust 800 based on screen width
        double y = Math.random() * 600; // Adjust 600 based on screen height

        // Define bonus trash with 5 seconds visibility
        Trash bonusTrash = new Trash(x, y, true, 5000); // true = bonus trash, 5000ms duration
        bonusTrash.setNbPoints(50); // Assign higher points for bonus trash

        // Add bonus trash to the trash list
        localTrashset.add(bonusTrash);

        System.out.println("Bonus trash spawned at (" + x + ", " + y + ")");
    }



    public void updateLocalTrashe(int id) {
        TrashDB dataBaseTrash = trashDAO.findById(id);    // Récupération du déchet a mettre à jour
        Trash localTrash = localTrashset.get(id);         // Récupération du déchet local

        double x = dataBaseTrash.getX();
        double y = dataBaseTrash.getY();
        int visible = dataBaseTrash.getVisible();

        if (!(x == localTrash.getX() || !(y == localTrash.getY()) || !(visible == localTrash.getVisible()))) {
            Trash trashCopy = localTrashset.get((int) id);
            trashCopy.setX(x); // Mise à jour de la position X
            trashCopy.setY(y); // Mise à jour de la position Y
            trashCopy.setVisible(visible); // Mise à jour de la visibilité
            localTrashset.set((int) id, trashCopy); // Remplacement de l'ancien déchet par le nouveau
        }
    }

    // Vérification de la collision entre un déchet et l'avatar
    private boolean isColliding(Trash trash, Avatar avatar) {
        return trash.getX() < avatar.getX() + avatar.getWidth() &&
               trash.getX() + trash.getWidth() > avatar.getX() &&
               trash.getY() < avatar.getY() + avatar.getHeight() &&
               trash.getY() + trash.getHeight() > avatar.getY();
    }
    // Vérification des collisions avec les bords de l'écran
    public void checkCollisionWithPanel() {
        if (myAvatar.getX() > backgroundImage.getWidth() - myAvatar.getWidth()) {
            myAvatar.setX(0);
        }
        if (myAvatar.getX() < 0) {
            myAvatar.setX(backgroundImage.getWidth() - myAvatar.getWidth());
        }
        if (myAvatar.getY() > backgroundImage.getHeight() - myAvatar.getHeight()) {
            myAvatar.setY(backgroundImage.getHeight() - myAvatar.getHeight());
        }
        if (myAvatar.getY() < 0) {
            myAvatar.setY(0);
        }
    }

    // Vérification de collision entre deux déchets
    public static boolean checkCollisionBetweenTrashes(Trash trash1, Trash trash2) {
        return (trash2.getX() <= trash1.getX() + trash1.getWidth() + 10 &&
                trash1.getX() <= trash2.getX() + trash2.getWidth() + 10 &&
                trash1.getY() <= trash2.getY() + trash2.getHeight() + 10 &&
                trash2.getY() <= trash1.getY() + trash1.getHeight() + 10);
    }

    // Initialisation des déchets
    public void initTrashes() {
        this.trashDAO.clear();
        List<Trash> initialTrashset = new ArrayList<>();

        // Création de déchets aléatoires
        for (int i = 0; i < NB_TRASHES; i++) {
            Trash trash = createRandomTrash(i);
            if (trash != null) {
                trash.updatePosition();             // Mise à jour de la position du déchet
                initialTrashset.add(trash);         // Ajout du déchet à la liste
            }
        }

        // Enregistrement des déchets initiaux dans la base de données
        for (Trash trash : initialTrashset) {
            trashDAO.create(trash.convertTrashToTrashDB());
        }
    }

    // Initialisation des déchets locaux à partir de la base de données
    public void initLocalTrashes() {
        List<TrashDB> dataBaseTrashset = trashDAO.findAll();            // Récupération des déchets de la base de données
        localTrashset = new ArrayList<>();                              // Liste pour les déchets locaux

        // Création des objets Trash en fonction des données de la base
        for (TrashDB trash : dataBaseTrashset) {
            double x = trash.getX();
            double y = trash.getY();
            switch (trash.getName()){
                case "Boat" :
                    localTrashset.add(new Boat(x,y));
                    break;
                case "Bottle":
                    localTrashset.add(new Bottle(x,y));
                    break;
                case "Can":
                    localTrashset.add(new Can(x,y));
                    break;
                case "OilContainer":
                    localTrashset.add(new OilContainer(x,y));
                    break;
                case "PlasticBag":
                    localTrashset.add(new PlasticBag(x,y));
                    break;
                case "Tire":
                    localTrashset.add(new Tire(x,y));
                    break;
            }
        }
    }

    // Création d'un déchet aléatoire en fonction de l'index
    private Trash createRandomTrash(int index) {
        int randomNumber = randomNbr.nextInt(1, 3); // Choisit un nombre entre 1 et 2 aléatoirement
        if (index <= 15) {
            return (randomNumber == 1) ? new Bottle(index) : new Can(index);
        } else if (index <= 25) {
            return (randomNumber == 1) ? new PlasticBag(index) : new Tire(index);
        } else {
            return (randomNumber == 1) ? new OilContainer(index) : new Boat(index);
        }
    }

    // Getters et Setters
    public Avatar getMyAvatar() {
        return this.myAvatar;
    }

    public DiverDAO getDiverDAO() {
        return diverDAO;
    }

    public List<Diver> getAllDivers() {
        return allDivers;
    }

    public Diver getDiver() {
        return diver;
    }

    public void setDiver(Diver diver) {
        this.diver = diver;
    }

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(BufferedImage backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public void setDiverDAO(DiverDAO diverDAO) {
        this.diverDAO = diverDAO;
    }

    public void setAllDivers(List<Diver> allDivers) {
        this.allDivers = allDivers;
    }

    public TrashDAO getTrashDAO() {
        return trashDAO;
    }

    public void setTrashDAO(TrashDAO trashDAO) {
        this.trashDAO = trashDAO;
    }

    public List<Trash> getLocalTrashset() {
        return localTrashset;
    }

    public void setLocalTrashset(List<Trash> localTrashset) {
        this.localTrashset = localTrashset;
    }

    public Random getRandomNbr() {
        return randomNbr;
    }
}
