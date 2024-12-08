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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.firestore.UpdateHandler.getUpdate;

public class PathOfExile2Handler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update newsHandler() throws ExecutionException, InterruptedException, IOException {
    Update newsPost = retrievePostDetails.getPathOfExile2News();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("111");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "Path of Exile 2 Steam hub");
  }

  public Update hotfixHandler() throws IOException, ExecutionException, InterruptedException {
      Update patchPost = retrievePostDetails.getPathOfExile2Hotfix();

      Firestore database = FirestoreClient.getFirestore();

      DocumentReference docRef = database.collection("games").document("113");

      return getUpdate(patchPost, docRef, firestoreDocUpdater, "Path of Exile 2 hotfixes");
  }

  public Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          PathOfExile2Handler poe2Handler = new PathOfExile2Handler();
          try {
            Update newsPost = poe2Handler.newsHandler();
            if (newsPost != null) {
              getPathOfExile2Update(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching path of exile 2 post: " + e.getMessage());
          }
        });
  }

  public Mono<Void> runHotfixTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          PathOfExile2Handler poe2Handler = new PathOfExile2Handler();
          try {
            Update patchPost = poe2Handler.hotfixHandler();
            if (patchPost != null) {
              getPathOfExile2Hotfix(gateway, patchPost);
            }
          } catch (Exception e) {
            System.err.println(
                "Error while fetching path of exile 2 patch/hotfix post: " + e.getMessage());
          }
        });
  }

  private void getPathOfExile2Update(GatewayDiscordClient gateway, Update post) {
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

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author(
                          "Path of Exile 2, Steam News Hub",
                          "https://store.steampowered.com/news/app/2694490",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/poe2-logo.png?raw=true")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  private void getPathOfExile2Hotfix(GatewayDiscordClient gateway, Update post) {
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
                      .author(
                          "Path of Exile 2, " + post.getAuthor(),
                          "https://www.pathofexile.com/forum/view-forum/2212",
                          "")
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
    return runNewsTask(gateway).then(runHotfixTask(gateway));
  }
}
