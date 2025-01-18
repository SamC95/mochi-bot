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

public class TheOldRepublicHandler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  private Update newsHandler() throws ExecutionException, InterruptedException, IOException {
    Update newsPost = retrievePostDetails.getTheOldRepublicNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("108");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "Star Wars The Old Republic");
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          TheOldRepublicHandler theOldRepublicHandler = new TheOldRepublicHandler();
          try {
            Update newsPost = theOldRepublicHandler.newsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
              System.err.printf(
                      "[%s] [ERROR] Failed to fetch Star Wars: The Old Republic update: %s\n",
                      LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("SWTOR_CHANNEL_ID");
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
                      .author("Star Wars: The Old Republic", "https://www.swtor.com/info/news", "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/swtorlogo.png?raw=true")
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
