package com.mochibot.handlers;

import com.mochibot.utils.posts.DateFormatter;
import com.mochibot.utils.posts.GameHandler;
import com.mochibot.utils.loaders.PropertiesLoader;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.mochi.scraper.model.Update;
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

public class WarThunderHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseHandler databaseHandler;

  public WarThunderHandler(
      RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseHandler = databaseHandler;
  }

  private Update pinnedNewsHandler() throws SQLException, IOException {
    Update newsPost = retrievePostDetails.getWarThunderPinnedNews();

    return databaseHandler.getUpdate(newsPost, "War Thunder news (pinned)", 114);
  }

  private Update unpinnedNewsHandler() throws IOException, SQLException {
    Update newsPost = retrievePostDetails.getWarThunderUnpinnedNews();

    return databaseHandler.getUpdate(newsPost, "War Thunder news (unpinned)", 109);
  }

  private Update pinnedChangelogHandler() throws IOException, SQLException {
    Update changelogPost = retrievePostDetails.getWarThunderPinnedChangelog();

    return databaseHandler.getUpdate(changelogPost, "War Thunder changelog (pinned)", 119);
  }

  private Update unpinnedChangelogHandler() throws IOException, SQLException {
    Update changelogPost = retrievePostDetails.getWarThunderUnpinnedChangelog();

    return databaseHandler.getUpdate(changelogPost, "War Thunder changelog (unpinned)", 120);
  }

  private Mono<Void> runPinnedNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler =
              new WarThunderHandler(retrievePostDetails, databaseHandler);

          try {
            Update newsPost = warThunderHandler.pinnedNewsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost, "War Thunder News", "https://warthunder.com/en/news");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch War Thunder pinned news: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runUnpinnedNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler =
              new WarThunderHandler(retrievePostDetails, databaseHandler);

          try {
            Update newsPost = warThunderHandler.unpinnedNewsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost, "War Thunder News", "https://warthunder.com/en/news");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch War Thunder unpinned news: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runPinnedChangelogTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler =
              new WarThunderHandler(retrievePostDetails, databaseHandler);

          try {
            Update changelogPost = warThunderHandler.pinnedChangelogHandler();

            if (changelogPost != null) {
              postUpdate(
                  gateway,
                  changelogPost,
                  "War Thunder Changelog",
                  "https://warthunder.com/en/game/changelog/");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch War Thunder pinned changelog: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runUnpinnedChangelogTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WarThunderHandler warThunderHandler =
              new WarThunderHandler(retrievePostDetails, databaseHandler);

          try {
            Update changelogPost = warThunderHandler.unpinnedChangelogHandler();

            if (changelogPost != null) {
              postUpdate(
                  gateway,
                  changelogPost,
                  "War Thunder Changelog",
                  "https://warthunder.com/en/game/changelog/");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch War Thunder unpinned changelog: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(
      GatewayDiscordClient gateway, Update post, String authorName, String authorUrl) {
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
                      .author(authorName, authorUrl, "")
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
    return runUnpinnedNewsTask(gateway)
        .then(runPinnedNewsTask(gateway)
                .then(runPinnedChangelogTask(gateway)
                        .then(runUnpinnedChangelogTask(gateway))));
  }
}
