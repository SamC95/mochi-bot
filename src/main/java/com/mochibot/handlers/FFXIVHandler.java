package com.mochibot.handlers;

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

public class FFXIVHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseHandler databaseHandler;

  public FFXIVHandler(RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseHandler = databaseHandler;
  }

  private Update topicsHandler() throws IOException, SQLException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXIVTopics();

    return databaseHandler.getUpdate(topicsPost, "Final Fantasy XIV topics", 100);
  }

  private Update newsHandler() throws IOException, SQLException {
    Update newsPost = retrievePostDetails.getFinalFantasyXIVNews();

    return databaseHandler.getUpdate(newsPost, "Final Fantasy XIV news", 101);
  }

  private Mono<Void> runTopicsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler(retrievePostDetails, databaseHandler);
          try {
            Update topicsPost = xivHandler.topicsHandler();
            if (topicsPost != null) {
              postUpdate(gateway, topicsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XIV Lodestone topics update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler(retrievePostDetails, databaseHandler);
          try {
            Update newsPost = xivHandler.newsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch Final Fantasy XIV Lodestone news update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("FFXIV_CHANNEL_ID");
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
                          "Final Fantasy XIV: The Lodestone",
                          "https://eu.finalfantasyxiv.com/lodestone/",
                          "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail("https://lodestonenews.com/images/thumbnail.png")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

  @Override
  public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
    return runTopicsTask(gateway).then(runNewsTask(gateway));
  }
}
