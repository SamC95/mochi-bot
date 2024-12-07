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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.example.mochibot.utils.UpdateHandler.getUpdate;

public class PathOfExile2Handler {
    RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
    FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

    public Update newsHandler() throws ExecutionException, InterruptedException, IOException {
        Update newsPost = retrievePostDetails.getPathOfExile2News();

        Firestore database = FirestoreClient.getFirestore();

        DocumentReference docRef = database.collection("games").document("111");

        return getUpdate(newsPost, docRef, firestoreDocUpdater, "Path of Exile 2");
    }

    public Mono<Void> runNewsTask(GatewayDiscordClient gateway) {
        return Mono.fromRunnable(() -> {
            PathOfExile2Handler poe2Handler = new PathOfExile2Handler();
            try {
                Update newsPost = poe2Handler.newsHandler();
                if (newsPost != null) {
                    getPathOfExile2Update(gateway, newsPost);
                }
            }
            catch (Exception e) {
                System.err.println("Error while fetching path of exile 2 post: " + e.getMessage());
            }
        });
    }

    private void getPathOfExile2Update(GatewayDiscordClient gateway, Update post) {
        var channelId = PropertiesLoader.loadProperties("POE2_CHANNEL_ID");

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
                                                    "Path of Exile 2, Steam News Hub",
                                                    "https://store.steampowered.com/news/app/2694490",
                                                    "")
                                            .title(post.getTitle())
                                            .url(post.getUrl())
                                            .image(image)
                                            .description(post.getDescription())
                                            .thumbnail(
                                                    "https://github.com/SamC95/news-scraper/blob/master/src/main/resources/thumbnails/poe2-logo.png?raw=true")
                                            .timestamp(Instant.now())
                                            .build();
                            return channel.createMessage(embed);
                        })
                .subscribe();
    }
}
