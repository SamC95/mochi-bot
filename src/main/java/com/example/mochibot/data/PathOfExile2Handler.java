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

public class PathOfExile2Handler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final FirestoreDocUpdater firestoreDocUpdater;

  public PathOfExile2Handler(
      RetrievePostDetails retrievePostDetails, FirestoreDocUpdater firestoreDocUpdater) {
    this.retrievePostDetails = retrievePostDetails;
    this.firestoreDocUpdater = firestoreDocUpdater;
  }

  private Update newsHandler() throws ExecutionException, InterruptedException, IOException {
    Update newsPost = retrievePostDetails.getPathOfExile2News();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("111");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "Path of Exile 2 Steam hub");
  }

  private Update hotfixHandler() throws IOException, ExecutionException, InterruptedException {
    Update patchPost = retrievePostDetails.getPathOfExile2Hotfix();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("113");

    return getUpdate(patchPost, docRef, firestoreDocUpdater, "Path of Exile 2 hotfixes");
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          PathOfExile2Handler poe2Handler =
              new PathOfExile2Handler(retrievePostDetails, firestoreDocUpdater);
          try {
            Update newsPost = poe2Handler.newsHandler();
            if (newsPost != null) {
              postUpdate(
                  gateway,
                  newsPost,
                  "https://store.steampowered.com/news/app/2694490",
                  "Steam News");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Path of Exile 2 steam news update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  // Currently unused functionality
  private Mono<Void> runHotfixTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          PathOfExile2Handler poe2Handler =
              new PathOfExile2Handler(retrievePostDetails, firestoreDocUpdater);
          try {
            Update patchPost = poe2Handler.hotfixHandler();
            if (patchPost != null) {
              postUpdate(
                  gateway,
                  patchPost,
                  "https://www.pathofexile.com/forum/view-forum/2212",
                  patchPost.getAuthor());
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Path of Exile 2 patch/hotfix update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(
      GatewayDiscordClient gateway, Update post, String authorUrl, String category) {
    var channelId = PropertiesLoader.loadProperties("POE2_CHANNEL_ID");
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

              String description =
                  post.getDescription() != null
                          && !Objects.equals(post.getDescription(), "No description available")
                      ? post.getDescription()
                      : "";

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author("Path of Exile 2: " + category, authorUrl, "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(description)
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/poe2-logo.png?raw=true")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  @Override
  public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
    return runNewsTask(gateway);
  }
}
