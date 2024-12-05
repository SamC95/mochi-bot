package com.example.mochibot;

import com.example.mochibot.utils.FirestoreBuilder;
import com.sun.tools.javac.Main;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

public class MochiBotApplication {
  public static void main(String[] args) {
    String discordToken;

    try {
      FirestoreBuilder firestoreBuilder = new FirestoreBuilder();
      firestoreBuilder.setUpFirebase();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

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

    Properties properties = new Properties();

    try (InputStream input =
        Main.class.getClassLoader().getResourceAsStream("application.properties")) {
      if (input == null) {
        System.out.println("Error retrieving application.properties");
        return;
      }

      properties.load(input);

      discordToken = properties.getProperty("DISCORD_TOKEN");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    DiscordClient mochiBot = DiscordClient.create(discordToken);

    Mono<Void> login =
        mochiBot.withGateway(
            (GatewayDiscordClient gateway) -> {
              // ReadyEvent example
              Mono<Void> printOnLogin =
                  gateway
                      .on(
                          ReadyEvent.class,
                          event ->
                              Mono.fromRunnable(
                                  () -> {
                                    final User self = event.getSelf();
                                    System.out.printf(
                                        "Logged in as %s#%s%n",
                                        self.getUsername(), self.getDiscriminator());
                                  }))
                      .then();

              // MessageCreateEvent example
              Mono<Void> handlePingCommand =
                  gateway
                      .on(
                          MessageCreateEvent.class,
                          event -> {
                            Message message = event.getMessage();

                            if (message.getContent().equalsIgnoreCase("!ping")) {
                              return message
                                  .getChannel()
                                  .flatMap(channel -> channel.createMessage(embed));
                            }

                            return Mono.empty();
                          })
                      .then();

              // combine them!
              return printOnLogin.and(handlePingCommand);
            });
    login.block();
  }
}
