package com.example.mochibot.data;

import com.example.mochibot.utils.FirestoreDocUpdater;
import com.example.mochibot.utils.PropertiesLoader;
import com.example.mochibot.utils.RetrievePostDetails;
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
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.UpdateHandler.getUpdate;

public class WarThunderHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update WarThunderNewsHandler()
      throws IOException, ExecutionException, InterruptedException {
    Update newsPost = retrievePostDetails.getWarThunderNews();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("109");

    return getUpdate(newsPost, docRef, firestoreDocUpdater);
  }

  public Mono<Void> runWarThunderTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler = new WarThunderHandler();

          try {
            Update newsPost = warThunderHandler.WarThunderNewsHandler();
            if (newsPost != null) {
              getWarThunderUpdate(gateway, newsPost);
            }
          }
          catch (Exception e) {
            System.err.println("Exception while fetching update: " + e.getMessage());
          }
        });
  }

  private void getWarThunderUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("WAR_THUNDER_CHANNEL_ID");

    gateway
        .getChannelById(Snowflake.of(channelId))
        .ofType(TextChannel.class)
        .flatMap(
            channel -> {
              String image = post.getImage() != null ? post.getImage() : "";

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author("War Thunder", "https://warthunder.com/en/news", "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/war-thunder-logo.png?raw=true")
                      .timestamp(Instant.now())
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }
}
