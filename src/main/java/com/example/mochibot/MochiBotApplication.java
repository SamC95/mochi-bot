package com.example.mochibot;

import com.example.mochibot.utils.FirestoreBuilder;
import com.example.mochibot.utils.PropertiesLoader;
import com.sun.tools.javac.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
    mochi.start();
  }
}
