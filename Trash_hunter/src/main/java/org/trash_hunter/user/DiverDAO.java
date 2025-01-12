package org.trash_hunter.user;

import org.trash_hunter.util.DataAccessObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;


public class DiverDAO extends DataAccessObject<Diver> {

    //Requêtes pour la table Diver
    private static final String INSERT = "INSERT INTO Diver (x, y, pseudo, score, score_max, color) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String GET_ONE = "SELECT id, x, y, pseudo, score, score_max, creation_date, game_time, color FROM Diver WHERE id = ?";
    private static final String UPDATE = "UPDATE Diver SET x = ?, y = ?, pseudo = ?, score = ?, score_max = ?, color = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM Diver WHERE id = ?";
    private static final String GET_ALL = "SELECT id, x, y, pseudo, score, score_max, creation_date, game_time, color FROM Diver";
    private static final String GET_ALL_PSEUDO_FROM_DIVER= "SELECT pseudo FROM Diver";

    //Requêtes pour la table Best_scores
    private static final String GET_ALL_PSEUDO_FROM_BEST_SCORE = "SELECT pseudo FROM Best_scores";
    private static final String INSERT_SCORE = "INSERT INTO Best_scores (x, y, pseudo, score, score_max, color) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String TRUNCATE_TABLE = "TRUNCATE TABLE Diver";
    public DiverDAO(Connection connection) {
        super(connection);
    }

    @Override
    public Diver findById(long id) {
        Diver diver = null;
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(GET_ONE)) {
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                diver = new Diver();
                diver.setId(resultSet.getLong("id"));
                diver.setX(resultSet.getFloat("x"));
                diver.setY(resultSet.getFloat("y"));
                diver.setPseudo(resultSet.getString("pseudo"));
                diver.setScore(resultSet.getInt("score"));
                diver.setScore_max(resultSet.getInt("score_max"));
                diver.setCreation_date(resultSet.getDate("creation_date"));
                diver.setGame_time(resultSet.getTime("game_time"));
                diver.setColor(resultSet.getString("color"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return diver;
    }

    @Override
    public List<Diver> findAll() {
        List<Diver> divers = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(GET_ALL)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Diver diver = new Diver();
                diver.setId(resultSet.getLong("id"));
                diver.setX(resultSet.getFloat("x"));
                diver.setY(resultSet.getFloat("y"));
                diver.setPseudo(resultSet.getString("pseudo"));
                diver.setScore(resultSet.getInt("score"));
                diver.setScore_max(resultSet.getInt("score_max"));
                diver.setCreation_date(resultSet.getDate("creation_date"));
                diver.setGame_time(resultSet.getTime("game_time"));
                diver.setColor(resultSet.getString("color"));
                divers.add(diver);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return divers;
    }
    public List<String> findAllPseudoFromDiver(){
        List<String> divers = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(GET_ALL_PSEUDO_FROM_DIVER)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Diver diver = new Diver();
                diver.setPseudo(resultSet.getString("pseudo"));
                divers.add(diver.getPseudo());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return divers;
    }

    public List<String> findAllPseudoFromBestScore() {
        List<String> divers = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(GET_ALL_PSEUDO_FROM_BEST_SCORE)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Diver diver = new Diver();
                diver.setPseudo(resultSet.getString("pseudo"));
                divers.add(diver.getPseudo());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return divers;
    }

    @Override
    public void update(Diver updatedDiver, long id) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(UPDATE)) {
            preparedStatement.setFloat(1, updatedDiver.getX());
            preparedStatement.setFloat(2, updatedDiver.getY());
            preparedStatement.setString(3, updatedDiver.getPseudo());
            preparedStatement.setInt(4, updatedDiver.getScore());
            preparedStatement.setInt(5, updatedDiver.getScore_max());
            preparedStatement.setString(6, updatedDiver.getColor());
            preparedStatement.setLong(7, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(Diver newDiver) {
        newDiver.setX(newDiver.getX());
        newDiver.setY(newDiver.getY());
        newDiver.setPseudo(newDiver.getPseudo());
        newDiver.setScore(newDiver.getScore());
        newDiver.setScore_max(newDiver.getScore_max());
        newDiver.setColor(newDiver.getColor());
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setFloat(1, newDiver.getX());
            preparedStatement.setFloat(2, newDiver.getY());
            preparedStatement.setString(3, newDiver.getPseudo());
            preparedStatement.setInt(4, newDiver.getScore());
            preparedStatement.setInt(5, newDiver.getScore_max());
            preparedStatement.setString(6, newDiver.getColor());

            // Exécute la mise à jour et obtient les clés générées
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating diver failed, no rows affected.");
            }

            // Récupère les clés générées
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newDiver.setId(generatedKeys.getLong(1)); // Assigne l'ID généré au nouveau Diver
                } else {
                    throw new SQLException("Creating diver failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(long id) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(DELETE)) {
            preparedStatement.setLong(1, id);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting diver failed, no rows affected.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void addToBestScores(Diver diver) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(INSERT_SCORE, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setFloat(1, diver.getX());
            preparedStatement.setFloat(2, diver.getY());
            preparedStatement.setString(3, diver.getPseudo());
            preparedStatement.setInt(4, diver.getScore());
            preparedStatement.setInt(5, diver.getScore_max());
            preparedStatement.setString(6, diver.getColor());

            // Exécute la mise à jour et obtient les clés générées
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating diver failed, no rows affected.");
            }

            // Récupère les clés générées
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    diver.setId(generatedKeys.getLong(1)); // Assigne l'ID généré au nouveau Diver
                } else {
                    throw new SQLException("Add diver to best scores failed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    @Override
    public void clear() {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(TRUNCATE_TABLE)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private static final String GET_TOP_SCORES = "SELECT id, pseudo, score_max, color FROM Diver ORDER BY score_max DESC LIMIT 3";

    public List<Diver> getTopScores() {
        List<Diver> topDivers = new ArrayList<>();
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(GET_TOP_SCORES)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Diver diver = new Diver();
                diver.setId(resultSet.getLong("id"));
                diver.setPseudo(resultSet.getString("pseudo"));
                diver.setScore_max(resultSet.getInt("score_max"));
                diver.setColor(resultSet.getString("color"));
                topDivers.add(diver);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch top scores", e);
        }
        return topDivers;
    }

}