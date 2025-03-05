package com.example;

import javax.swing.*;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/";

    public static Connection connect(String username, String password) throws SQLException {
        return DriverManager.getConnection(DB_URL + "MusicLibrary", username, password);
    }

    public static void initRoles(Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT public.init_roles()")) {
            stmt.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка инициализации ролей: " + e.getMessage());
        }
    }

    public static void createUser(Connection conn, String username, String password, String role) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT public.create_new_user(?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.execute();
            JOptionPane.showMessageDialog(null, "Пользователь " + username + " создан!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка создания пользователя: " + e.getMessage());
        }
    }

    // Создание базы данных
    public static void createDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE MusicLibrary");
            JOptionPane.showMessageDialog(null, "База данных создана!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка: " + e.getMessage());
        }
    }

    public static void deleteDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP DATABASE IF EXISTS MusicLibrary");

            ResultSet rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datname = 'MusicLibrary'");
            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Ошибка: база данных не была удалена.");
            } else {
                JOptionPane.showMessageDialog(null, "База данных удалена!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка: " + e.getMessage());
        }
    }
}
