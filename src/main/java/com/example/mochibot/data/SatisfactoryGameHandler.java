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

public class SatisfactoryGameHandler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update newsHandler() throws ExecutionException, InterruptedException, IOException {
    Update newsPost = retrievePostDetails.getSatisfactoryGameNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("107");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "SatisfactoryGame");
  }

  public Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          SatisfactoryGameHandler satisfactoryGameHandler = new SatisfactoryGameHandler();
          try {
            Update newsPost = satisfactoryGameHandler.newsHandler();
            if (newsPost != null) {
              getSatisfactoryUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching satisfactory game update: " + e.getMessage());
          }
        });
  }

  private void getSatisfactoryUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("SATISFACTORY_CHANNEL_ID");

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
                          "Satisfactory, Steam News Hub",
                          "https://store.steampowered.com/news/app/526870",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/satisfactory-logo.png?raw=true")
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
