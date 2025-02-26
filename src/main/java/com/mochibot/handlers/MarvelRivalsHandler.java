package com.mochibot.handlers;

import com.mochibot.utils.loaders.PropertiesLoader;
import com.mochibot.utils.posts.DateFormatter;
import com.mochibot.utils.posts.GameHandler;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.example.scraper.model.Update;
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


public class MarvelRivalsHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseHandler databaseHandler;

  public MarvelRivalsHandler(
      RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseHandler = databaseHandler;
  }

  private Update announcementHandler() throws SQLException, IOException {
    Update announcementPost = retrievePostDetails.getMarvelRivalsAnnouncements();

    return databaseHandler.getUpdate(announcementPost, "Marvel Rivals announcements", 115);
  }

  private Update devDiaryHandler() throws SQLException, IOException {
    Update devDiaryPost = retrievePostDetails.getMarvelRivalsDevDiaries();

    return databaseHandler.getUpdate(devDiaryPost, "Marvel Rivals dev diaries", 116);
  }

  private Update updateHandler() throws SQLException, IOException {
    Update updatePost = retrievePostDetails.getMarvelRivalsUpdates();

    return databaseHandler.getUpdate(updatePost, "Marvel Rivals updates", 117);
  }

  private Mono<Void> runAnnouncementTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          MarvelRivalsHandler marvelRivalsHandler =
              new MarvelRivalsHandler(retrievePostDetails, databaseHandler);
          try {
            Update announcementPost = marvelRivalsHandler.announcementHandler();
            if (announcementPost != null) {
              postUpdate(
                  gateway, announcementPost, "https://www.marvelrivals.com/news/", "Announcement");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch marvel rivals announcement post: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runDevDiaryTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          MarvelRivalsHandler marvelRivalsHandler =
              new MarvelRivalsHandler(retrievePostDetails, databaseHandler);
          try {
            Update devDiaryPost = marvelRivalsHandler.devDiaryHandler();
            if (devDiaryPost != null) {
              postUpdate(
                  gateway, devDiaryPost, "https://www.marvelrivals.com/devdiaries/", "Dev Diaries");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch marvel rivals dev diary post: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runUpdateTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          MarvelRivalsHandler marvelRivalsHandler =
              new MarvelRivalsHandler(retrievePostDetails, databaseHandler);
          try {
            Update updatePost = marvelRivalsHandler.updateHandler();
            if (updatePost != null) {
              postUpdate(
                  gateway, updatePost, "https://www.marvelrivals.com/gameupdate/", "Game Update");
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch marvel rivals update post: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(
      GatewayDiscordClient gateway, Update post, String authorUrl, String category) {
    var channelId = PropertiesLoader.loadProperties("MARVEL_RIVALS_CHANNEL_ID");
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
                      .author("Marvel Rivals: " + category, authorUrl, "")
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
