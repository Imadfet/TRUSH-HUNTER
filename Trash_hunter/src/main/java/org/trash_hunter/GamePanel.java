package org.trash_hunter;

import org.trash_hunter.user.Diver;
import org.trash_hunter.util.DatabaseConnection;
import org.trash_hunter.util.OutilsJDBC;
import org.trash_hunter.util.SoundManager;
import org.trash_hunter.windows.Start_window;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.SQLException;


public class GamePanel extends JFrame implements KeyListener, ActionListener, WindowListener {
    private BufferedImage backgroundImage;
    private Graphics2D contexte;
    private JLabel jLabel1;
    private Game game;
    private Timer timer;
    private JDialog diversDialog;

    public GamePanel(String pseudo, String color) throws SQLException{
        //init de la fenetre
        this.setSize(1440,780);
        this.setResizable(false);
        this.setTitle("Trash Hunter");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.jLabel1 = new JLabel();
        this.jLabel1.setPreferredSize(new Dimension(1440,780));
        this.setContentPane(this.jLabel1);
        //this.setFocusableWindowState(false);  // Empêche l'interraction avec la fenêtre
        this.pack();

        // Création du buffer pour l'affichage du jeu et récupération du contexte graphique
        this.backgroundImage = new BufferedImage(1440, 780, BufferedImage.TYPE_INT_ARGB);
        this.jLabel1.setIcon(new ImageIcon(backgroundImage));
        this.contexte = this.backgroundImage.createGraphics();

        //Creation du jeu
        this.game = new Game(pseudo,color);

        //Creation  du Timer qui appelle this.actionPerformed() tous les 40 ms
        this.timer=new Timer(20,this);
        this.timer.start();

        //Ajout du listener
        this.addKeyListener(this);
        this.addWindowListener(this);
    }
    public static void main (String[]args) throws SQLException {
        SoundManager.playBackgroundMusic("resources/sounds/background_music.wav");
        Start_window startWindow = new Start_window();   //Le GamePanel se lance dans ActionPerformed de la classe Start_window
    }

    private void showPodium() {
        java.util.List<Diver> topDivers = game.getDiverDAO().getTopScores(); // Fetch top players

        // Create the Podium panel
        JPanel podiumPanel = new JPanel();
        podiumPanel.setLayout(new GridLayout(4, 1)); // Layout for podium rows
        podiumPanel.add(new JLabel("🏆 Podium - Top 3 Players 🏆", SwingConstants.CENTER));

        // Add each player to the panel
        for (int i = 0; i < topDivers.size(); i++) {
            Diver diver = topDivers.get(i);
            String rank = (i == 0) ? "🥇" : (i == 1) ? "🥈" : "🥉";
            JLabel label = new JLabel(rank + " " + diver.getPseudo() + " - Score: " + diver.getScore());
            label.setHorizontalAlignment(SwingConstants.CENTER);
            podiumPanel.add(label);
        }

        // Show the Podium in a dialog
        JOptionPane.showMessageDialog(this, podiumPanel, "Podium", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDiversTable() {
        try {
            // Connexion à la base de données
            Connection connexion = DatabaseConnection.getConnection();
            String nomTable = "Diver";

            // Récupérer les résultats sous forme de chaîne
            String resultString = OutilsJDBC.showTableAsString(connexion, nomTable);

            // Créer un JTextArea pour afficher les résultats
            JTextArea textArea = new JTextArea(resultString);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            // Créer le JDialog
            diversDialog = new JDialog(this, "Joueurs actuels", Dialog.ModalityType.APPLICATION_MODAL);
            diversDialog.setSize(600, 300);
            diversDialog.setLocationRelativeTo(this);
            diversDialog.add(new JScrollPane(textArea)); // Ajoute le JTextArea dans le JDialog
            diversDialog.setVisible(true); // Affiche le JDialog

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la récupération des données : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void keyPressed (KeyEvent evt) {
        if (evt.getKeyCode() == 87 ||evt.getKeyCode() == KeyEvent.VK_UP) {
            this.game.getMyAvatar().setUp(true);
        } else if (evt.getKeyCode() == 83 || evt.getKeyCode()== KeyEvent.VK_DOWN) {
            this.game.getMyAvatar().setDown(true);
        } else if (evt.getKeyCode() == 65|| evt.getKeyCode()== KeyEvent.VK_LEFT) {
            this.game.getMyAvatar().setLeft(true);
        } else if (evt.getKeyCode() == 68 || evt.getKeyCode()== KeyEvent.VK_RIGHT) {
            this.game.getMyAvatar().setRight(true);
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {  // Ajout de la touche Échap
            int choix = JOptionPane.showConfirmDialog(this,
                    "Voulez-vous vraiment quitter le jeu ?",
                    "Quitter le jeu",
                    JOptionPane.YES_NO_OPTION);

            if (choix == JOptionPane.YES_OPTION) {
                //Action à venir effectuer avant la fermeture de la fenêtre
                this.game.getDiverDAO().delete(this.game.getMyAvatar().getId());
                this.game.getDiverDAO().addToBestScores(this.game.getDiver());
                DatabaseConnection.close();  // Arrête la conncetion à la base de donnée
                this.dispose(); //Ferme la fenêtre
                System.exit(0);
            }

        }
        else if (evt.getKeyCode() == KeyEvent.VK_R){    //Affiche le tableau des joueurs actuels lors de l'appui sur R
            showDiversTable();
        }


        else if (evt.getKeyCode() == KeyEvent.VK_P) { // Press P to show podium
                showPodium();
            }
        game.getDiverDAO().addToBestScores(game.getDiver()); // Save the current player's score

    }



    @Override
    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == 87 ||evt.getKeyCode() == KeyEvent.VK_UP) {
            this.game.getMyAvatar().setUp(false);
        } else if (evt.getKeyCode() == 83 || evt.getKeyCode()== KeyEvent.VK_DOWN) {
            this.game.getMyAvatar().setDown(false);
        } else if (evt.getKeyCode() == 65|| evt.getKeyCode()== KeyEvent.VK_LEFT) {
            this.game.getMyAvatar().setLeft(false);
        } else if (evt.getKeyCode() == 68 || evt.getKeyCode()== KeyEvent.VK_RIGHT) {
            this.game.getMyAvatar().setRight(false);
        } else if (evt.getKeyCode() == KeyEvent.VK_R){
            diversDialog.dispose();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            this.game.update();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        this.game.rendering(contexte);
        this.jLabel1.repaint();
        if (this.game.isFinished()) {
            this.timer.stop();
        }
    }
    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        //Action à venir effectuer avant la fermeture de la fenêtre
        this.game.getDiverDAO().delete(this.game.getDiver().getId());
        this.game.getDiverDAO().addToBestScores(this.game.getDiver());
        DatabaseConnection.close();  // Arrête la conncetion à la base de donnée
        System.exit((0));
    }

    @Override
    public void windowClosed(WindowEvent e) {
        this.game.getDiverDAO().delete(this.game.getDiver().getId());
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    public Graphics2D getContexte() {
        return contexte;
    }
}
