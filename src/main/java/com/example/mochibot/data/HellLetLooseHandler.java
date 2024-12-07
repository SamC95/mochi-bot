package com.example.mochibot.data;

import com.example.mochibot.utils.firestore.FirestoreDocUpdater;
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
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.firestore.UpdateHandler.getUpdate;

public class HellLetLooseHandler implements GameHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update newsHandler() throws IOException, ExecutionException, InterruptedException {
    Update newsPost = retrievePostDetails.getHellLetLooseNews();

    Firestore datastore = FirestoreClient.getFirestore();

    DocumentReference docRef = datastore.collection("games").document("104");

    return getUpdate(newsPost, docRef, firestoreDocUpdater, "Hell Let Loose");
  }

  public Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          HellLetLooseHandler hellLetLooseHandler = new HellLetLooseHandler();
          try {
            Update newsPost = hellLetLooseHandler.newsHandler();
            if (newsPost != null) {
              getHellLetLooseUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching Hell Let Loose update: " + e.getMessage());
          }
        });
  }

  private void getHellLetLooseUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("HLL_CHANNEL_ID");

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
                          "Hell Let Loose, Steam News Hub",
                          "https://store.steampowered.com/news/app/686810",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/hell-let-loose-logo.png?raw=true")
                      .timestamp(Instant.now())
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
