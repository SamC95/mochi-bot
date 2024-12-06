package com.example.mochibot.data;

import com.example.mochibot.utils.FirestoreDocUpdater;
import com.example.mochibot.utils.PropertiesLoader;
import com.example.mochibot.utils.RetrievePostDetails;
import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.UpdateHandler.getUpdate;

public class WorldOfWarcraftHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update newsHandler() throws ExecutionException, InterruptedException, IOException {
    Update newsPost = retrievePostDetails.getWorldOfWarcraftNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("110");

    return getUpdate(newsPost, docRef, firestoreDocUpdater);
  }

  public Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WorldOfWarcraftHandler worldOfWarcraftHandler = new WorldOfWarcraftHandler();
          try {
            Update newsPost = worldOfWarcraftHandler.newsHandler();
            if (newsPost != null) {
              getWorldOfWarcraftUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching world of warcraft update: " + e.getMessage());
          }
        });
  }

  private void getWorldOfWarcraftUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("WOW_CHANNEL_ID");

    gateway
        .getChannelById(Snowflake.of(channelId))
        .ofType(TextChannel.class)
        .flatMap(
            channel -> {
              String image = post.getImage() != null ? post.getImage() : "";

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author(
                          "World of Warcraft",
                          "https://worldofwarcraft.blizzard.com/en-gb/news",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/worldofwarcraft-logo.png?raw=true")
                      .timestamp(Instant.now())
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }
}
