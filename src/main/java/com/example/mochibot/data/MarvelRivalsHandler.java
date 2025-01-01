package com.example.mochibot.data;

import com.example.mochibot.utils.firestore.FirestoreDocUpdater;
import com.example.mochibot.utils.loaders.PropertiesLoader;
import com.example.mochibot.utils.posts.DateFormatter;
import com.example.mochibot.utils.posts.GameHandler;
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

public class MarvelRivalsHandler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update announcementHandler() throws ExecutionException, InterruptedException, IOException {
    Update announcementPost = retrievePostDetails.getMarvelRivalsAnnouncements();
    Firestore database = FirestoreClient.getFirestore();
    DocumentReference docRef = database.collection("games").document("115");

    return getUpdate(announcementPost, docRef, firestoreDocUpdater, "Marvel Rivals announcements");
  }

  public Update devDiaryHandler() throws ExecutionException, InterruptedException, IOException {
    Update devDiaryPost = retrievePostDetails.getMarvelRivalsDevDiaries();
    Firestore database = FirestoreClient.getFirestore();
    DocumentReference docRef = database.collection("games").document("116");

    return getUpdate(devDiaryPost, docRef, firestoreDocUpdater, "Marvel Rivals dev diaries");
  }

  public Update updateHandler() throws ExecutionException, InterruptedException, IOException {
    Update updatePost = retrievePostDetails.getMarvelRivalsUpdates();
    Firestore database = FirestoreClient.getFirestore();
    DocumentReference docRef = database.collection("games").document("117");

    return getUpdate(updatePost, docRef, firestoreDocUpdater, "Marvel Rivals updates");
  }

  public Mono<Void> runAnnouncementTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          MarvelRivalsHandler marvelRivalsHandler = new MarvelRivalsHandler();
          try {
            Update announcementPost = marvelRivalsHandler.announcementHandler();
            if (announcementPost != null) {
              getMarvelRivalsPost(gateway, announcementPost, "https://www.marvelrivals.com/news/");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch marvel rivals announcement post: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  public Mono<Void> runDevDiaryTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          MarvelRivalsHandler marvelRivalsHandler = new MarvelRivalsHandler();
          try {
            Update devDiaryPost = marvelRivalsHandler.devDiaryHandler();
            if (devDiaryPost != null) {
              getMarvelRivalsPost(
                  gateway, devDiaryPost, "https://www.marvelrivals.com/devdiaries/");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch marvel rivals dev diary post: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  public Mono<Void> runUpdateTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          MarvelRivalsHandler marvelRivalsHandler = new MarvelRivalsHandler();
          try {
            Update updatePost = marvelRivalsHandler.updateHandler();
            if (updatePost != null) {
              getMarvelRivalsPost(gateway, updatePost, "https://www.marvelrivals.com/gameupdate/");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch marvel rivals update post: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  public void getMarvelRivalsPost(GatewayDiscordClient gateway, Update post, String authorUrl) {
    var channelId = PropertiesLoader.loadProperties("MARVEL-RIVALS_CHANNEL_ID");
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
                      .author("Marvel Rivals", authorUrl, "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/marvel-rivals.png?raw=true")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  @Override
  public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
    return runAnnouncementTask(gateway).then(runDevDiaryTask(gateway).then(runUpdateTask(gateway)));
  }
}
