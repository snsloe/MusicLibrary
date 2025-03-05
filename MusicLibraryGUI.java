package com.example;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;

public class MusicLibraryGUI {
    private JFrame frame;
    private JTextField titleField, artistField, albumField, genreField, durationField;
    private JButton addButton, updateButton, deleteButton, searchButton;
    private JButton createDbButton, deleteDbButton, clearTableButton;
    private MusicLibraryDAO dao;

    public MusicLibraryGUI(Connection connection) {
        dao = new MusicLibraryDAO(connection);

        frame = new JFrame("Музыкальная библиотека");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(0, 2, 10, 10));

        frame.add(new JLabel("Название:"));
        titleField = new JTextField();
        frame.add(titleField);

        frame.add(new JLabel("Исполнитель:"));
        artistField = new JTextField();
        frame.add(artistField);

        frame.add(new JLabel("Альбом:"));
        albumField = new JTextField();
        frame.add(albumField);

        frame.add(new JLabel("Жанр:"));
        genreField = new JTextField();
        frame.add(genreField);

        frame.add(new JLabel("Длительность (сек):"));
        durationField = new JTextField();
        frame.add(durationField);

        addButton = new JButton("Добавить трек");
        addButton.addActionListener(this::addTrack);
        frame.add(addButton);

        searchButton = new JButton("Поиск");
        searchButton.addActionListener(this::searchTrack);
        frame.add(searchButton);

        updateButton = new JButton("Обновить трек");
        updateButton.addActionListener(this::updateTrack);
        frame.add(updateButton);

        deleteButton = new JButton("Удалить по названию");
        deleteButton.addActionListener(this::deleteTrack);
        frame.add(deleteButton);


        createDbButton = new JButton("Создать базу данных");
        createDbButton.addActionListener(e -> {
            DatabaseManager.createDatabase(dao.getConnection());
        });
        frame.add(createDbButton);


        deleteDbButton = new JButton("Удалить базу данных");
        deleteDbButton.addActionListener(e -> {
            DatabaseManager.deleteDatabase(dao.getConnection());
        });
        frame.add(deleteDbButton);

        clearTableButton = new JButton("Очистить таблицу");
        clearTableButton.addActionListener(this::clearTable);
        frame.add(clearTableButton);

        JButton showAllButton = new JButton("Показать все треки");
        showAllButton.addActionListener(this::showAllTracks);
        frame.add(showAllButton);

        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
                System.exit(0);
            }
        });
    }

    private void addTrack(ActionEvent e) {
        try {
            dao.addRecord(titleField.getText(), artistField.getText(), albumField.getText(), genreField.getText(), Integer.parseInt(durationField.getText()));
            JOptionPane.showMessageDialog(frame, "Трек добавлен!");

            // Очищаем поля после добавления
            titleField.setText("");
            artistField.setText("");
            albumField.setText("");
            genreField.setText("");
            durationField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
        }
    }

    private void searchTrack(ActionEvent e) {
        try {
            String title = JOptionPane.showInputDialog(frame, "Введите название песни для поиска:");
            List<String> results = dao.searchByTitle(title);
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ничего не найдено.");
            } else {
                JOptionPane.showMessageDialog(frame, String.join("\n", results));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
        }
    }

    private void updateTrack(ActionEvent e) {
        try {
            int id = Integer.parseInt(JOptionPane.showInputDialog(frame, "Введите ID трека для обновления:"));

            // Получаем текущие данные трека
            String sql = "SELECT * FROM public.search_by_id(?)";
            String currentTitle = "", currentArtist = "", currentAlbum = "", currentGenre = "";
            int currentDuration = 0;

            try (PreparedStatement stmt = dao.getConnection().prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentTitle = rs.getString("title");
                    currentArtist = rs.getString("artist");
                    currentAlbum = rs.getString("album");
                    currentGenre = rs.getString("genre");
                    currentDuration = rs.getInt("duration");

                    JOptionPane.showMessageDialog(frame, "Текущий трек:\n" +
                            id + " - " + currentTitle + " - " + currentArtist + " - " +
                            currentAlbum + " - " + currentGenre + " - " + currentDuration);
                } else {
                    JOptionPane.showMessageDialog(frame, "Трек с таким ID не найден.");
                    return;
                }
            }

            // Получаем новые данные (если пустые - оставляем старые значения)
            String newTitle = JOptionPane.showInputDialog(frame, "Введите новое название:", currentTitle);
            String newArtist = JOptionPane.showInputDialog(frame, "Введите нового исполнителя:", currentArtist);
            String newAlbum = JOptionPane.showInputDialog(frame, "Введите новый альбом:", currentAlbum);
            String newGenre = JOptionPane.showInputDialog(frame, "Введите новый жанр:", currentGenre);
            int newDuration = Integer.parseInt(JOptionPane.showInputDialog(frame, "Введите новую длительность:", currentDuration));

            // Вызываем SQL-функцию обновления
            dao.updateRecord(id, newTitle, newArtist, newAlbum, newGenre, newDuration);
            JOptionPane.showMessageDialog(frame, "Трек обновлен!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
        }
    }

    private void deleteTrack(ActionEvent e) {
        String title = JOptionPane.showInputDialog(frame, "Введите название трека для удаления:");
        boolean deleted = dao.deleteByTitle(title);
        if (deleted) {
            JOptionPane.showMessageDialog(frame, "Трек удален!");
        } else {
            JOptionPane.showMessageDialog(frame, "Трек не найден!");
        }
    }

    private void clearTable(ActionEvent e) {
        try {
            dao.clearRecords();
            JOptionPane.showMessageDialog(frame, "Таблица очищена!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
        }
    }

    private void showAllTracks(ActionEvent e) {
        try {
            List<String> results = dao.getAllTracks();

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "База данных пуста.");
            } else {
                StringBuilder resultText = new StringBuilder();
                for (String result : results) {
                    resultText.append(result).append("\n");
                }
                JOptionPane.showMessageDialog(frame, resultText.toString());
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (dao.getConnection() != null && !dao.getConnection().isClosed()) {
                dao.getConnection().close();
                System.out.println("Соединение закрыто.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Администратор", "Гость"};
            int choice = JOptionPane.showOptionDialog(null, "Выберите режим входа:", "Выбор режима",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            String username, password;
            if (choice == 0) { // Администратор
                username = JOptionPane.showInputDialog("Введите имя администратора:");
                password = JOptionPane.showInputDialog("Введите пароль администратора:");
            } else { // Гость
                username = JOptionPane.showInputDialog("Введите имя гостя:");
                password = JOptionPane.showInputDialog("Введите пароль гостя:");
            }

            if (username != null && password != null) {
                try {
                    Connection conn = DatabaseManager.connect(username, password);
                    DatabaseManager.initRoles(conn);
                    new MusicLibraryGUI(conn);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Ошибка подключения: " + ex.getMessage());
                }
            }
        });
    }
}
