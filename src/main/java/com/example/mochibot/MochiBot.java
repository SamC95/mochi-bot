package com.example.mochibot;

import com.example.mochibot.data.FFXIVHandler;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

public class MochiBot {
  private final String token;

  FFXIVHandler xivHandler = new FFXIVHandler();

  public MochiBot(String token) {
    this.token = token;
  }

  public void start() {
    DiscordClient client = DiscordClient.create(token);
    Mono<Void> login = client.withGateway(this::initialiseGateway);
    login.block();
  }

  private Mono<Void> initialiseGateway(GatewayDiscordClient gateway) {

    Schedulers.parallel()
        .schedulePeriodically(
            () -> xivHandler.runFFXIVTopicsTask(gateway).subscribe(), 0, 10, TimeUnit.MINUTES);

    Schedulers.parallel()
        .schedulePeriodically(
            () -> xivHandler.runFFXIVNewsTask(gateway).subscribe(), 1, 10, TimeUnit.MINUTES);

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
}
