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

public class DragonwildsHandler implements GameHandler {
  private final RetrievePostDetails retrievePostDetails;
  private final DatabaseHandler databaseHandler;

  public DragonwildsHandler(
      RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
    this.retrievePostDetails = retrievePostDetails;
    this.databaseHandler = databaseHandler;
  }

  private Update newsHandler() throws SQLException, IOException {
    Update newsPost = retrievePostDetails.getDragonwildsNews();

    return databaseHandler.getUpdate(newsPost, "RuneScape Dragonwilds", 131);
  }

  private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          DragonwildsHandler dragonwildsHandler =
              new DragonwildsHandler(retrievePostDetails, databaseHandler);

          try {
            Update newsPost = dragonwildsHandler.newsHandler();
            if (newsPost != null) {
              postUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
            System.err.printf(
                "[%s] [ERROR] Failed to fetch RuneScape Dragonwilds update: %s\n",
                LocalTime.now(), e.getMessage());
          }
        });
  }

  private void postUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("RS_DRAGONWILDS_CHANNEL_ID");
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
                      .author("RuneScape Dragonwilds", "https://dragonwilds.runescape.com/news", "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(description)
                      .thumbnail(
                          "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/rs-dragonwilds-logo.png?raw=true")
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
