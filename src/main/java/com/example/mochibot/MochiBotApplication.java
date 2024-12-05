package com.example.mochibot;

import com.example.mochibot.utils.FirestoreBuilder;
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

    String discordToken = loadDiscordToken();

    MochiBot mochi = new MochiBot(discordToken);
    mochi.start();


              /*try {
                Update topicsPost = ffxivHandler.FFXIVTopicsHandler();
              } catch (IOException | ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
              }*/
  }

    private static String loadDiscordToken() {
        Properties properties = new Properties();

        try (InputStream input = MochiBotApplication.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Error retrieving application.properties");
            }
            properties.load(input);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        return properties.getProperty("DISCORD_TOKEN");
    }
}
/*
EmbedCreateSpec embed =
        EmbedCreateSpec.builder()
                .color(Color.BLUE)
                .title("Pulse: FINAL FANTASY XIV Remix Album Now Available on Streaming Services!")
                .url(
                        "https://eu.finalfantasyxiv.com/lodestone/topics/detail/eed477fbfe105c2e45c90229f6773f8cfd964f2f")
                .author(
                        "FINAL FANTASY XIV, The Lodestone", "https://eu.finalfantasyxiv.com/lodestone/", "")
                .description(
                        "Following its release in September 2020, Pulse: FINAL FANTASY XIV Remix Album is now available on streaming services! Enjoy 14 popular tracks vibrantly recreated in EDM, house, techno pop, and more! Streaming Services Read on for a list of streaming services. * Song availability may vary depending...\n")
                .thumbnail("https://lodestonenews.com/images/thumbnail.png")
                .image(
                        "https://img.finalfantasyxiv.com/t/eed477fbfe105c2e45c90229f6773f8cfd964f2f.jpg?1733210385?1733119284")
                .timestamp(Instant.now())
                .build();
*/
