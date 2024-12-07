package com.example.mochibot.data;

import com.example.mochibot.utils.firestore.FirestoreDocUpdater;
import com.example.mochibot.utils.posts.GameHandler;
import com.example.mochibot.utils.loaders.PropertiesLoader;
import com.example.mochibot.utils.posts.RetrievePostDetails;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.firestore.UpdateHandler.getUpdate;

public class OSRSHandler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update newsHandler() throws ExecutionException, InterruptedException, IOException {
    Update newsPost = retrievePostDetails.getOldSchoolRuneScapeNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("112");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "Old School RuneScape");
  }

  public Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          OSRSHandler osrsHandler = new OSRSHandler();
          try {
            Update newsPost = osrsHandler.newsHandler();
            if (newsPost != null) {
              getOSRSUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching old school runescape post: " + e.getMessage());
          }
        });
  }

  private void getOSRSUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("OSRS_CHANNEL_ID");

    gateway
        .getChannelById(Snowflake.of(channelId))
        .ofType(TextChannel.class)
        .flatMap(
            channel -> {
              String image =
                  post.getImage() != null && !Objects.equals(post.getImage(), "No image found")
                      ? post.getImage()
                      : "";

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author(
                          "Old School RuneScape, " + post.getCategory(),
                          "https://secure.runescape.com/m=news/archive?oldschool=1",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/osrs-logo.png?raw=true")
                      .timestamp(Instant.now())
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

    @Override
    public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
        return runNewsTask(gateway);    }
}
