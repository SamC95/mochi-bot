package com.example.mochibot;

import com.example.mochibot.utils.repository.firestore.FirestoreBuilder;
import com.example.mochibot.utils.loaders.PropertiesLoader;

import java.io.IOException;
import java.time.LocalTime;

/*
  Firestore must be set up to use this program and the required mochi-bot.json file added
  application.properties is not included, which includes the discord token and channel ids
  These must be implemented for the program to work. Are not provided due to sensitivity of that data.
*/

public class MochiBotApplication {
  public static void main(String[] args) {
    try {
      FirestoreBuilder firestoreBuilder = new FirestoreBuilder();
      firestoreBuilder.setUpFirebase();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    // Not included in GitHub repo
    String discordToken = PropertiesLoader.loadProperties("DISCORD_TOKEN");

    MochiBot mochi = new MochiBot(discordToken);

    Thread mochiThread = new Thread(mochi::start);
    mochiThread.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.out.printf("[%s] [SYSTEM] Stopping MochiBot...", LocalTime.now())));
  }
}
