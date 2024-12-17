package com.example.mochibot;

import com.example.mochibot.utils.firestore.FirestoreBuilder;
import com.example.mochibot.utils.loaders.PropertiesLoader;

import java.io.IOException;
import java.time.LocalTime;

public class MochiBotApplication {
  public static void main(String[] args) {
    try {
      FirestoreBuilder firestoreBuilder = new FirestoreBuilder();
      firestoreBuilder.setUpFirebase();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    String discordToken = PropertiesLoader.loadProperties("DISCORD_TOKEN");

    MochiBot mochi = new MochiBot(discordToken);

    Thread mochiThread = new Thread(mochi::start);
    mochiThread.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     System.out.printf("[%s] [SYSTEM] Stopping MochiBot...", LocalTime.now());
    }));
  }
}
