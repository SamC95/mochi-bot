package com.mochibot.data;

import com.mochibot.utils.repository.firestore.FirestoreBuilder;
import com.mochibot.utils.repository.firestore.FirestoreDocUpdater;
import com.mochibot.utils.posts.DateFormatter;
import com.mochibot.utils.posts.GameHandler;
import com.mochibot.utils.loaders.PropertiesLoader;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.mochibot.utils.repository.UpdateHandler.getUpdate;

public class FFXIHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final FirestoreDocUpdater firestoreDocUpdater;
  private final FirestoreBuilder firestoreBuilder;

  public FFXIHandler(
      RetrievePostDetails retrievePostDetails, FirestoreDocUpdater firestoreDocUpdater, FirestoreBuilder firestoreBuilder) {
    this.retrievePostDetails = retrievePostDetails;
    this.firestoreDocUpdater = firestoreDocUpdater;
    this.firestoreBuilder = firestoreBuilder;
  }

  // Topics news feed
  private Update topicsHandler() throws IOException, ExecutionException, InterruptedException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXITopics();

    Firestore database = firestoreBuilder.getFirestore();

    DocumentReference docRef = database.collection("games").document("102");

    return getUpdate(topicsPost, docRef, firestoreDocUpdater, "Final Fantasy XI topics");
  }

  // Information news feed
  private Update informationHandler() throws IOException, ExecutionException, InterruptedException {
    Update informationPost = retrievePostDetails.getFinalFantasyXIInformation();

    Firestore database = firestoreBuilder.getFirestore();

    DocumentReference docRef = database.collection("games").document("103");

    return getUpdate(informationPost, docRef, firestoreDocUpdater, "Final Fantasy XI information");
  }

  private Mono<Void> runTopicsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIHandler xiHandler = new FFXIHandler(retrievePostDetails, firestoreDocUpdater, firestoreBuilder);
          try {
            Update topicsPost = xiHandler.topicsHandler();
            if (topicsPost != null) {
              postUpdate(gateway, topicsPost);
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
          FFXIHandler xiHandler = new FFXIHandler(retrievePostDetails, firestoreDocUpdater, firestoreBuilder);
          try {
            Update informationPost = xiHandler.informationHandler();
            if (informationPost != null) {
              postUpdate(gateway, informationPost);
            }
          } catch (IOException | ExecutionException | InterruptedException e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XI PlayOnline information update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
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
