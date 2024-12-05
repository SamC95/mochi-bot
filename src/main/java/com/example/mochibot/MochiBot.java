package com.example.mochibot;

import com.example.mochibot.data.FFXIVHandler;
import com.example.scraper.model.Update;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class MochiBot {
  private final String token;

  public MochiBot(String token) {
    this.token = token;
  }

  public void start() {
    DiscordClient client = DiscordClient.create(token);
    Mono<Void> login = client.withGateway(this::initialiseGateway);
    login.block();
  }

  private Mono<Void> initialiseGateway(GatewayDiscordClient gateway) {

    Schedulers.parallel().schedulePeriodically(
            () -> runFFXIVUpdateTask(gateway).subscribe(), 0, 10, TimeUnit.MINUTES
    );

    return Mono.when(handleReadyEvent(gateway), handlePingCommand(gateway));
  }

  private Mono<Void> handlePingCommand(GatewayDiscordClient gateway) {
    return gateway
        .on(
            MessageCreateEvent.class,
            event -> {
              Message message = event.getMessage();

              if (message.getContent().equalsIgnoreCase("!ping")) {
                return message.getChannel().flatMap(channel -> channel.createMessage("pong!"));
              }
              return Mono.empty();
            })
        .then();
  }

  private Mono<Void> handleReadyEvent(GatewayDiscordClient gateway) {
    return gateway
        .on(
            ReadyEvent.class,
            event ->
                Mono.fromRunnable(
                    () -> {
                      User self = event.getSelf();
                      System.out.printf(
                          "Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
                    }))
        .then();
  }

  private Mono<Void> runFFXIVUpdateTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler();
          try {
            Update topicsPost = xivHandler.FFXIVTopicsHandler();
            if (topicsPost != null) {
              getFFXIVTopicUpdate(gateway, topicsPost);
            }
          } catch (Exception e) {
            System.err.println("Error while fetching lodestone update: " + e.getMessage());
          }
        });
  }

  private void getFFXIVTopicUpdate(GatewayDiscordClient gateway, Update topicsPost) {
    gateway
        .getChannelById(Snowflake.of("placeholder"))
        .ofType(TextChannel.class)
        .flatMap(
            channel -> {
              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author(
                          topicsPost.getAuthor(), "https://eu.finalfantasyxiv.com/lodestone/", "")
                      .title(topicsPost.getTitle())
                      .url(topicsPost.getUrl())
                      .image(topicsPost.getImage())
                      .description(topicsPost.getDescription())
                      .thumbnail("https://lodestonenews.com/images/thumbnail.png")
                      .timestamp(Instant.now())
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }
}
