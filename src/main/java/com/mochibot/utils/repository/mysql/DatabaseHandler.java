package com.mochibot.utils.repository.mysql;

import com.mochi.scraper.model.Update;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class DatabaseHandler {
  Connection connection = DatabaseBuilder.getConnection();

  public Update getUpdate(Update post, String gameTitle, int gameId) throws SQLException {
    if (post == null || post.getTitle() == null || post.getUrl() == null) {
      System.err.printf(
              "[%s] [ERROR] Post data for %s is invalid or missing, skipping update.\n",
              LocalTime.now(), gameTitle);

      return null;
    }

    String query = "SELECT title, url FROM posts WHERE title = ? OR url = ?";

    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, post.getTitle());
      statement.setString(2, post.getUrl());

      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        String currentTitle = resultSet.getString("title");
        String currentUrl = resultSet.getString("url");

        boolean isSameTitle = Objects.equals(currentTitle, post.getTitle());
        boolean isSameUrl = Objects.equals(currentUrl, post.getUrl());

        if (isSameTitle || isSameUrl) {
          System.out.printf("[%s] [INFO] No new post for %s\n", LocalTime.now(), gameTitle);
        }
      } else {
        insertPost(post, gameId, gameTitle);
        return post;
      }
    }

    return null;
  }

  private void insertPost(Update post, int gameId, String gameTitle) {
    String insertQuery =
        "INSERT INTO posts (game_id, game_name, author, title, description, image_url, url, post_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    LocalDate localDate = LocalDate.now();
    Date sqlDate = Date.valueOf(localDate);

    try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
      statement.setInt(1, gameId);
      statement.setString(2, gameTitle);
      statement.setString(3, post.getAuthor());
      statement.setString(4, post.getTitle());
      statement.setString(5, post.getDescription());
      statement.setString(6, post.getImage());
      statement.setString(7, post.getUrl());
      statement.setDate(8, sqlDate);

      statement.executeUpdate();

      System.out.printf("[%s] [INFO] Inserted new post for %s\n", LocalTime.now(), gameTitle);
    } catch (SQLException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to insert new post for %s: %s\n", LocalTime.now(), gameTitle, e);
    }
  }
}
