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

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Objects;

public class WutheringWavesHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseHandler databaseHandler;

  public WutheringWavesHandler(
      RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseHandler = databaseHandler;
  }

  private Update newsHandler() throws SQLException {
    Update newsPost = retrievePostDetails.getWutheringWavesNews();

    return databaseHandler.getUpdate(newsPost, "Wuthering Waves", 129);
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          WutheringWavesHandler wutheringWavesHandler =
              new WutheringWavesHandler(retrievePostDetails, databaseHandler);

          try {
            Update newsPost = wutheringWavesHandler.newsHandler();

            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception ex) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Wuthering Waves update: %s",
                LocalTime.now(), ex.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("WUWA_CHANNEL_ID");
    String formattedDate = DateFormatter.getFormattedDate();

    gateway
        .getChannelById(Snowflake.of(channelId))
        .ofType(TextChannel.class)
        .flatMap(
            channel -> {
              String image =
                  post.getImage() != null && !Objects.equals(post.getImage(), "")
                      ? post.getImage()
                      : "";

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author(
                          "Wuthering Waves: " + post.getCategory(),
                          "https://wutheringwaves.kurogames.com/en/main#news",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/wuthering-waves-logo.png?raw=true")
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
