package com.mochibot;

import com.mochibot.utils.loaders.PropertiesLoader;
import com.mochibot.utils.repository.mysql.DatabaseBuilder;

import java.sql.Connection;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MochiBotApplication {
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public static void main(String[] args) {
    // Not included in GitHub repo
    String discordToken = PropertiesLoader.loadProperties("DISCORD_TOKEN");

    MochiBot mochi = new MochiBot(discordToken);

    Thread mochiThread = new Thread(mochi::start);
    mochiThread.start();

    // Handles periodic deletion of old posts from the database (6 months old)
    scheduler.scheduleAtFixedRate(
        () -> {
          DatabaseBuilder dbBuilder = new DatabaseBuilder();
          Connection connection = DatabaseBuilder.getConnection();

          if (connection != null) {
            System.out.printf("[%s] [SYSTEM] Beginning old post deletion task\n", LocalTime.now());
            dbBuilder.deleteOldPosts(connection);
            System.out.printf("[%s] [SYSTEM] Post deletion task finished\n", LocalTime.now());
            dbBuilder.closeConnection(connection);
          }
        }, 0, 24, TimeUnit.HOURS);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> System.out.printf("[%s] [SYSTEM] Stopping MochiBot...", LocalTime.now())));
  }
}
