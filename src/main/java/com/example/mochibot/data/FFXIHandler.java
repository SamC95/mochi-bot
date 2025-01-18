package com.example.mochibot.data;

import com.example.mochibot.utils.repository.firestore.FirestoreDocUpdater;
import com.example.mochibot.utils.posts.DateFormatter;
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
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.repository.UpdateHandler.getUpdate;

public class FFXIHandler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  // Topics news feed
  private Update topicsHandler() throws IOException, ExecutionException, InterruptedException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXITopics();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("102");

    return getUpdate(topicsPost, docRef, firestoreDocUpdater, "Final Fantasy XI topics");
  }

  // Information news feed
  private Update informationHandler() throws IOException, ExecutionException, InterruptedException {
    Update informationPost = retrievePostDetails.getFinalFantasyXIInformation();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("103");

    return getUpdate(informationPost, docRef, firestoreDocUpdater, "Final Fantasy XI information");
  }

  private Mono<Void> runTopicsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIHandler xiHandler = new FFXIHandler();
          try {
            Update topicsPost = xiHandler.topicsHandler();
            if (topicsPost != null) {
              getFFXIUpdate(gateway, topicsPost);
            }
          } catch (IOException | ExecutionException | InterruptedException e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XI PlayOnline topics update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runInformationTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIHandler xiHandler = new FFXIHandler();
          try {
            Update informationPost = xiHandler.informationHandler();
            if (informationPost != null) {
              getFFXIUpdate(gateway, informationPost);
            }
          } catch (IOException | ExecutionException | InterruptedException e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XI PlayOnline information update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void getFFXIUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("FFXI_CHANNEL_ID");
    String formattedDate = DateFormatter.getFormattedDate();

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
                          "FINAL FANTASY XI, PlayOnline",
                          "http://www.playonline.com/ff11eu/index.shtml",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/ffxi-logo-icon.png?raw=true")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  @Override
  public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
    return runTopicsTask(gateway).then(runInformationTask(gateway));
  }
}
