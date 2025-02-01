package com.mochibot.data;

import com.mochibot.utils.posts.DateFormatter;
import com.mochibot.utils.posts.GameHandler;
import com.mochibot.utils.loaders.PropertiesLoader;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.example.scraper.model.Update;
import com.mochibot.utils.repository.mysql.DatabaseBuilder;
import com.mochibot.utils.repository.mysql.DatabaseHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class FFXIHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseBuilder databaseBuilder;
  private final DatabaseHandler databaseHandler;

  public FFXIHandler(
      RetrievePostDetails retrievePostDetails, DatabaseBuilder databaseBuilder, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseBuilder = databaseBuilder;
    this.databaseHandler = databaseHandler;
  }

  // Topics news feed
  private Update topicsHandler() throws IOException, ExecutionException, InterruptedException, SQLException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXITopics();

    return databaseHandler.getUpdate(topicsPost, "Final Fantasy XI topics", 102);
  }

  // Information news feed
  private Update informationHandler() throws IOException, ExecutionException, InterruptedException, SQLException {
    Update informationPost = retrievePostDetails.getFinalFantasyXIInformation();

    return databaseHandler.getUpdate(informationPost, "Final Fantasy XI information", 103);
  }

  private Mono<Void> runTopicsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIHandler xiHandler = new FFXIHandler(retrievePostDetails, databaseBuilder, databaseHandler);
          try {
            Update topicsPost = xiHandler.topicsHandler();
            if (topicsPost != null) {
              postUpdate(gateway, topicsPost);
            }
          } catch (IOException | ExecutionException | InterruptedException | SQLException e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XI PlayOnline topics update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runInformationTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIHandler xiHandler = new FFXIHandler(retrievePostDetails, databaseBuilder, databaseHandler);
          try {
            Update informationPost = xiHandler.informationHandler();
            if (informationPost != null) {
              postUpdate(gateway, informationPost);
            }
          } catch (IOException | ExecutionException | InterruptedException | SQLException e) {
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
