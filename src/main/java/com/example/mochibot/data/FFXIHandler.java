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

public class FFXIHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update FFXITopicsHandler() throws IOException, ExecutionException, InterruptedException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXITopics();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("102");

    return getUpdate(topicsPost, docRef, firestoreDocUpdater);
  }

  public Update FFXIInformationHandler()
      throws IOException, ExecutionException, InterruptedException {
    Update informationPost = retrievePostDetails.getFinalFantasyXIInformation();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("103");

    return getUpdate(informationPost, docRef, firestoreDocUpdater);
  }

  public Mono<Void> runFFXITopicsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIHandler xiHandler = new FFXIHandler();
          try {
            Update topicsPost = xiHandler.FFXITopicsHandler();
            if (topicsPost != null) {
                getFFXIUpdate(gateway, topicsPost);
            }
          }
          catch (IOException | ExecutionException | InterruptedException e) {
            System.err.println("Error while fetching PlayOnline update: " + e.getMessage());
          }
        });
  }

  public Mono<Void> runFFXIInformationTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIHandler xiHandler = new FFXIHandler();
          try {
            Update informationPost = xiHandler.FFXIInformationHandler();
            if (informationPost != null) {
                getFFXIUpdate(gateway, informationPost);
            }
          }
          catch (IOException | ExecutionException | InterruptedException e) {
            System.err.println("Error while fetching PlayOnline update: " + e.getMessage());
          }
        });
  }

  public void getFFXIUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("FFXI_CHANNEL_ID");

    gateway
        .getChannelById(Snowflake.of(channelId))
        .ofType(TextChannel.class)
        .flatMap(
            channel -> {
              String image = post.getImage() != null ? post.getImage() : "";

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author(
                          "FINAL FANTASY XI, PlayOnline",
                          "http://www.playonline.com/ff11eu/index.shtml",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/final-fantasy-xi-logo.png?raw=true")
                      .timestamp(Instant.now())
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }
}
