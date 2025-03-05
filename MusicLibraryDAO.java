package com.example;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MusicLibraryDAO {
    private final Connection connection;

    public MusicLibraryDAO(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void clearRecords() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT public.clear_records()")) {
            stmt.execute();
        }
    }

    public List<String> searchByTitle(String title) throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM public.search_by_title(?)")) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                results.add("ID: " + rs.getInt("record_id") + " - " +
                        "Название: " + rs.getString("title") + " - " +
                        "Исполнитель: " + rs.getString("artist") + " - " +
                        "Альбом: " + rs.getString("album") + " - " +
                        "Жанр: " + rs.getString("genre") + " - " +
                        "Длительность: " + rs.getInt("duration"));
            }
        }
        return results;
    }

    public void addRecord(String title, String artist, String album, String genre, int duration) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT public.add_record(?, ?, ?, ?, ?)")) {
            stmt.setString(1, title);
            stmt.setString(2, artist);
            stmt.setString(3, album);
            stmt.setString(4, genre);
            stmt.setInt(5, duration);
            stmt.execute();
            JOptionPane.showMessageDialog(null, "Трек добавлен!");
        } catch (SQLException e) {
            if ("42501".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(null, "В режиме гостя нельзя добавлять записи.");
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка: " + e.getMessage());
            }
        }
    }

    public void updateRecord(int id, String title, String artist, String album, String genre, int duration) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT public.update_record(?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, id);
            stmt.setString(2, title);
            stmt.setString(3, artist);
            stmt.setString(4, album);
            stmt.setString(5, genre);
            stmt.setInt(6, duration);
            stmt.execute();
            JOptionPane.showMessageDialog(null, "Трек обновлен!");
        } catch (SQLException e) {
            if ("42501".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(null, "В режиме гостя нельзя обновлять записи.");
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка: " + e.getMessage());
            }
        }
    }

    public boolean deleteByTitle(String title) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT public.delete_by_title(?)")) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            if ("42501".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(null, "В режиме гостя нельзя удалять записи.");
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка: " + e.getMessage());
            }
        }
        return false;
    }

    public List<String> getAllTracks() throws SQLException {
        List<String> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM public.get_all_tracks()");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add("ID: " + rs.getInt("record_id") + " - " +
                        "Название: " + rs.getString("title") + " - " +
                        "Исполнитель: " + rs.getString("artist") + " - " +
                        "Альбом: " + rs.getString("album") + " - " +
                        "Жанр: " + rs.getString("genre") + " - " +
                        "Длительность: " + rs.getInt("duration"));
            }
        }
        return results;
    }


}
