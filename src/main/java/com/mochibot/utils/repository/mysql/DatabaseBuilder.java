package com.mochibot.utils.repository.mysql;

import com.mochibot.utils.loaders.PropertiesLoader;

import java.sql.*;
import java.time.LocalTime;

public class DatabaseBuilder {
  private static final String URL = PropertiesLoader.loadProperties("SQL_CONNECTION");
  private static final String USER = PropertiesLoader.loadProperties("SQL_USER");
  private static final String PASS = PropertiesLoader.loadProperties("SQL_PASSWORD");

  public static Connection getConnection() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");

      Connection connection = DriverManager.getConnection(URL, USER, PASS);
      System.out.printf(
          "[%s] [SQL] Successfully connected to database: %s\n", LocalTime.now(), connection);

      return connection;
    }
    catch (ClassNotFoundException | SQLException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to load MySQL JDBC Driver: %s\n", LocalTime.now(), e.getMessage());
    }

    return null;
  }

  public void deleteOldPosts(Connection connection) {
    String deleteQuery = "DELETE FROM posts WHERE post_date < DATE_SUB(NOW(), INTERVAL 2 MONTH)";

    try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
      int rowsAffected = statement.executeUpdate();

      System.out.printf("[%s] [INFO] Deleted %d old posts from the database.\n", LocalTime.now(), rowsAffected);
    }
    catch (SQLException e) {
      System.err.printf("[%s] [ERROR] Failed to delete old posts: %s\n", LocalTime.now(), e.getMessage());
    }
  }

  // For testing connection
  public void fetchPosts(Connection connection) {
    String query = "SELECT * FROM posts";

    try (Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query)) {

      while (resultSet.next()) {
        int gameId = resultSet.getInt("game_id");

        if (resultSet.getString("title") != null) {
          String title = resultSet.getString("title").trim();

          System.out.println("gameId: " + gameId);
          System.out.println("title: " + title);
        }
      }
    } catch (SQLException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to fetch posts: %s\n", LocalTime.now(), e.getMessage());
    }
  }

  public void closeConnection(Connection connection) {
    try {
      if (connection != null && !connection.isClosed()) {
        connection.close();
        System.out.printf(
            "[%s] [SQL] Database connection closed: %s\n", LocalTime.now(), connection);
      }
    } catch (SQLException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to close database connection: %s\n ",
          LocalTime.now(), e.getMessage());
    }
  }
}
