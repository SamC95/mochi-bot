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

public class WarThunderHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final FirestoreDocUpdater firestoreDocUpdater;

  public WarThunderHandler(
      RetrievePostDetails retrievePostDetails, FirestoreDocUpdater firestoreDocUpdater) {
    this.retrievePostDetails = retrievePostDetails;
    this.firestoreDocUpdater = firestoreDocUpdater;
  }

  private Update pinnedNewsHandler() throws ExecutionException, InterruptedException, IOException {
    Update newsPost = retrievePostDetails.getWarThunderPinnedNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("114");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "War Thunder (pinned)");
  }

  private Update unpinnedNewsHandler()
      throws IOException, ExecutionException, InterruptedException {
    Update newsPost = retrievePostDetails.getWarThunderUnpinnedNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("109");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "War Thunder (unpinned)");
  }

  private Mono<Void> runPinnedNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler =
              new WarThunderHandler(retrievePostDetails, firestoreDocUpdater);

          try {
            Update newsPost = warThunderHandler.pinnedNewsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch War Thunder update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runUnpinnedNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler =
              new WarThunderHandler(retrievePostDetails, firestoreDocUpdater);

          try {
            Update newsPost = warThunderHandler.unpinnedNewsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch War Thunder update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("WAR_THUNDER_CHANNEL_ID");
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
                      .author("War Thunder", "https://warthunder.com/en/news", "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/war-thunder-logo.png?raw=true")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  @Override
  public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
    return runUnpinnedNewsTask(gateway).then(runPinnedNewsTask(gateway));
  }
}
