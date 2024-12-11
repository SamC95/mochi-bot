package com.example.mochibot.data;

import com.example.mochibot.utils.firestore.FirestoreDocUpdater;
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

import static com.example.mochibot.utils.firestore.UpdateHandler.getUpdate;

public class WarThunderHandler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update WarThunderPinnedNewsHandler() throws ExecutionException, InterruptedException, IOException {
      Update newsPost = retrievePostDetails.getWarThunderPinnedNews();

      Firestore database = FirestoreClient.getFirestore();

      DocumentReference docRef = database.collection("games").document("114");

      return getUpdate(newsPost, docRef, firestoreDocUpdater, "War Thunder (pinned)");
  }

  public Update WarThunderUnpinnedNewsHandler()
      throws IOException, ExecutionException, InterruptedException {
    Update newsPost = retrievePostDetails.getWarThunderUnpinnedNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("109");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "War Thunder (unpinned)");
  }

  public Mono<Void> runWarThunderPinnedTask(GatewayDiscordClient gateway) {
      return Mono.fromRunnable(
              () -> {
                  WarThunderHandler warThunderHandler = new WarThunderHandler();

                  try {
                      Update newsPost = warThunderHandler.WarThunderPinnedNewsHandler();
                      if (newsPost != null) {
                          getWarThunderUpdate(gateway, newsPost);
                      }
                  } catch (Exception e) {
                      System.err.printf(
                              "[%s] [ERROR] Failed to fetch War Thunder update: %s\n",
                              LocalTime.now(), e.getMessage());
                  }
              });
  }

  public Mono<Void> runWarThunderUnpinnedTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler = new WarThunderHandler();

          try {
            Update newsPost = warThunderHandler.WarThunderUnpinnedNewsHandler();
            if (newsPost != null) {
              getWarThunderUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
              System.err.printf(
                      "[%s] [ERROR] Failed to fetch War Thunder update: %s\n",
                      LocalTime.now(), e.getMessage());
          }
        });
  }

  private void getWarThunderUpdate(GatewayDiscordClient gateway, Update post) {
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
    return runWarThunderUnpinnedTask(gateway).then(runWarThunderPinnedTask(gateway));
  }
}
