package com.mochibot.data;

import com.mochibot.utils.posts.DateFormatter;
import com.mochibot.utils.posts.GameHandler;
import com.mochibot.utils.loaders.PropertiesLoader;
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

public class OSRSHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseHandler databaseHandler;

  public OSRSHandler(
      RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseHandler = databaseHandler;
  }

  private Update newsHandler() throws SQLException, IOException {
    Update newsPost = retrievePostDetails.getOldSchoolRuneScapeNews();


    return databaseHandler.getUpdate(newsPost, "Old School RuneScape", 112);
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          OSRSHandler osrsHandler = new OSRSHandler(retrievePostDetails, databaseHandler);
          try {
            Update newsPost = osrsHandler.newsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Old School RuneScape update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("OSRS_CHANNEL_ID");
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
                          "OSRS: " + post.getCategory(),
                          "https://secure.runescape.com/m=news/archive?oldschool=1",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/osrs-logo.png?raw=true")
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
