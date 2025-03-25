package com.mochibot.handlers;

import com.mochi.scraper.model.Update;
import com.mochibot.utils.loaders.PropertiesLoader;
import com.mochibot.utils.posts.DateFormatter;
import com.mochibot.utils.posts.GameHandler;
import com.mochibot.utils.posts.RetrievePostDetails;
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

public class OverwatchHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseHandler databaseHandler;

  public OverwatchHandler(
      RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseHandler = databaseHandler;
  }

  private Update newsHandler() throws SQLException, IOException {
    Update newsPost = retrievePostDetails.getOverwatchNews();

    return databaseHandler.getUpdate(newsPost, "Overwatch 2 News", 106);
  }

  private Update patchHandler() throws SQLException, IOException {
    Update patchPost = retrievePostDetails.getOverwatchPatchNotes();

    return databaseHandler.getUpdate(patchPost, "Overwatch 2 Patch Notes", 123);
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          OverwatchHandler overwatchHandler =
              new OverwatchHandler(retrievePostDetails, databaseHandler);

          try {
            Update newsPost = overwatchHandler.newsHandler();

            if (newsPost != null) {
              postUpdate(
                  gateway,
                  newsPost,
                  "Overwatch 2 News",
                  "https://overwatch.blizzard.com/en-us/news/");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Overwatch 2 news update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runPatchNotesTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          OverwatchHandler overwatchHandler =
              new OverwatchHandler(retrievePostDetails, databaseHandler);

          try {
            Update newsPost = overwatchHandler.patchHandler();

            if (newsPost != null) {
              postUpdate(
                  gateway,
                  newsPost,
                  "Overwatch 2 Patch Notes",
                  "https://overwatch.blizzard.com/en-us/news/patch-notes/");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Overwatch 2 patch notes: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(
      GatewayDiscordClient gateway, Update post, String authorName, String authorUrl) {
    var channelId = PropertiesLoader.loadProperties("OVERWATCH_CHANNEL_ID");
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
                      .author(authorName, authorUrl, "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(description)
                      .thumbnail(
                          "https://raw.githubusercontent.com/SamC95/news-scraper/refs/heads/master/src/main/resources/thumbnails/overwatch-logo.png")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  @Override
  public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
    return runNewsTask(gateway).then(runPatchNotesTask(gateway));
  }
}
