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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.UpdateHandler.getUpdate;

public class FFXIVHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update FFXIVTopicsHandler() throws IOException, ExecutionException, InterruptedException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXIVTopics();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("100");

    return getUpdate(topicsPost, docRef, firestoreDocUpdater);
  }

  public Update FFXIVNewsHandler() throws IOException, ExecutionException, InterruptedException {
    Update newsPost = retrievePostDetails.getFinalFantasyXIVNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("101");

    return getUpdate(newsPost, docRef, firestoreDocUpdater);
  }

  public Mono<Void> runFFXIVTopicsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler();
          try {
            Update topicsPost = xivHandler.FFXIVTopicsHandler();
            if (topicsPost != null) {
              getFFXIVUpdate(gateway, topicsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching lodestone update: " + e.getMessage());
          }
        });
  }

  public Mono<Void> runFFXIVNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler();
          try {
            Update newsPost = xivHandler.FFXIVNewsHandler();
            if (newsPost != null) {
              getFFXIVUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching lodestone update: " + e.getMessage());
          }
        });
  }

  private void getFFXIVUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("FFXIV_CHANNEL_ID");

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
                      .author(post.getAuthor(), "https://eu.finalfantasyxiv.com/lodestone/", "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail("https://lodestonenews.com/images/thumbnail.png")
                      .timestamp(Instant.now())
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }
}
