package com.mochibot.data;

import com.mochibot.utils.repository.firestore.FirestoreDocUpdater;
import com.mochibot.utils.posts.DateFormatter;
import com.mochibot.utils.posts.GameHandler;
import com.mochibot.utils.loaders.PropertiesLoader;
import com.mochibot.utils.posts.RetrievePostDetails;
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

import static com.mochibot.utils.repository.UpdateHandler.getUpdate;

public class FFXIVHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final FirestoreDocUpdater firestoreDocUpdater;

  public FFXIVHandler(
      RetrievePostDetails retrievePostDetails, FirestoreDocUpdater firestoreDocUpdater) {
    this.retrievePostDetails = retrievePostDetails;
    this.firestoreDocUpdater = firestoreDocUpdater;
  }

  private Update topicsHandler() throws IOException, ExecutionException, InterruptedException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXIVTopics();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("100");

    return getUpdate(topicsPost, docRef, firestoreDocUpdater, "Final Fantasy XIV topics");
  }

  private Update newsHandler() throws IOException, ExecutionException, InterruptedException {
    Update newsPost = retrievePostDetails.getFinalFantasyXIVNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("101");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "Final Fantasy XIV news");
  }

  private Mono<Void> runTopicsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler(retrievePostDetails, firestoreDocUpdater);
          try {
            Update topicsPost = xivHandler.topicsHandler();
            if (topicsPost != null) {
              postUpdate(gateway, topicsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XIV Lodestone topics update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler(retrievePostDetails, firestoreDocUpdater);
          try {
            Update newsPost = xivHandler.newsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XIV Lodestone news update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("FFXIV_CHANNEL_ID");
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
                      .author(post.getAuthor(), "https://eu.finalfantasyxiv.com/lodestone/", "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail("https://lodestonenews.com/images/thumbnail.png")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  @Override
  public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
    return runTopicsTask(gateway).then(runNewsTask(gateway));
  }
}
