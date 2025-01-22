package com.mochibot;

import com.mochibot.utils.posts.PostScheduler;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.time.LocalTime;

public class MochiBot {
  private final String token;
  PostScheduler postScheduler = new PostScheduler();

  public MochiBot(String token) {
    this.token = token;
  }

  public void start() {
    DiscordClient client = DiscordClient.create(token);
    Mono<Void> login = client.withGateway(this::initialiseGateway);
    login.block();
  }

  private Mono<Void> initialiseGateway(GatewayDiscordClient gateway) {
    postScheduler.schedulePeriodicPosts(gateway);

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
                          "[%s] [INFO] Logged in as %s#%s%n",
                          LocalTime.now(), self.getUsername(), self.getDiscriminator());
                    }))
        .then();
  }
}
