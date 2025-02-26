package com.mochibot.data;

import com.example.scraper.model.Update;
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

public class KillingFloor3Handler implements GameHandler {
    private final RetrievePostDetails retrievePostDetails;
    private final DatabaseHandler databaseHandler;

    public KillingFloor3Handler(RetrievePostDetails retrievePostDetails, DatabaseHandler databaseHandler) {
        this.retrievePostDetails = retrievePostDetails;
        this.databaseHandler = databaseHandler;
    }

    private Update newsHandler() throws SQLException, IOException {
        Update newsPost = retrievePostDetails.getKillingFloor3News();

        return databaseHandler.getUpdate(newsPost, "Killing Floor 3", 121);
    }

    private Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
        return Mono.fromRunnable(() -> {
            KillingFloor3Handler killingFloor3Handler = new KillingFloor3Handler(retrievePostDetails, databaseHandler);

            try {
                Update newsPost = killingFloor3Handler.newsHandler();

                if (newsPost != null) {
                    postUpdate(gateway, newsPost);
                }
            }
            catch (Exception e) {
                System.err.printf("[%s] [ERROR] Failed to fetch Killing Floor 3 steam news update: %s\n", LocalTime.now(), e.getMessage());
            }
        });
    }

    private void postUpdate(GatewayDiscordClient gateway, Update post) {
        var channelId = PropertiesLoader.loadProperties("KILLING_FLOOR_3_CHANNEL_ID");
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
                                            .author("Killing Floor 3: Steam News",
                                                    "https://store.steampowered.com/news/app/1430190", "")
                                            .title(post.getTitle())
                                            .url(post.getUrl())
                                            .image(image)
                                            .description(post.getDescription())
                                            .thumbnail("https://raw.githubusercontent.com/SamC95/news-scraper/refs/heads/master/src/main/resources/thumbnails/kf3logo.jpg")
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
