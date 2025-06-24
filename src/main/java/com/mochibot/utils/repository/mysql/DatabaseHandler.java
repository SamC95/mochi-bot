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

    String query = "SELECT game_id, title, url FROM posts WHERE url = ?";

    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, post.getUrl());

      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        int existingGameId = resultSet.getInt("game_id");

        if (existingGameId != gameId) {
          updateGameIdForPost(post.getUrl(), gameId, gameTitle);
        }

        System.out.printf("[%s] [INFO] No new post for %s\n", LocalTime.now(), gameTitle);
        return null;
      }
      else {
        boolean postInserted = insertPost(post, gameId, gameTitle);

        if (postInserted) return post;
        else {
          System.out.printf(
              "[%s] [INFO] Insert skipped for %s (duplicate or error handled)\n",
              LocalTime.now(), gameTitle);
          return null;
        }
      }
    }
  }

  private boolean insertPost(Update post, int gameId, String gameTitle) {
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
      return true;
    } catch (SQLException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to insert new post for %s: %s\n", LocalTime.now(), gameTitle, e);
      return false;
    }
  }

  private void updateGameIdForPost(String url, int gameId, String gameTitle) {
    String updateQuery = "UPDATE posts SET game_id = ? WHERE url = ?";

    try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
      statement.setInt(1, gameId);
      statement.setString(2, url);

      int rowsUpdated = statement.executeUpdate();

      if (rowsUpdated > 0) {
        System.out.printf(
            "[%s] [INFO] Updated game_id for existing post of [%s] to [%d]\n",
            LocalTime.now(), gameTitle, gameId);
      } else {
        System.err.printf(
            "[%s] [ERROR] Failed to update game_id for post of [%s]\n", LocalTime.now(), gameTitle);
      }
    } catch (SQLException e) {
      System.err.printf(
          "[%s] [ERROR] An unexpected SQL exception occurred whilst updating the game id for [%s]\n",
          LocalTime.now(), gameTitle);
    }
  }
}
