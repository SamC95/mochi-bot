package com.example.mochibot;

import com.example.mochibot.data.FFXIHandler;
import com.example.mochibot.data.FFXIVHandler;
import com.example.mochibot.data.WarThunderHandler;
import com.example.mochibot.data.WorldOfWarcraftHandler;
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
  FFXIHandler xiHandler = new FFXIHandler();
  WarThunderHandler warThunderHandler = new WarThunderHandler();
  WorldOfWarcraftHandler worldOfWarcraftHandler = new WorldOfWarcraftHandler();

  public MochiBot(String token) {
    this.token = token;
  }

  public void start() {
    DiscordClient client = DiscordClient.create(token);
    Mono<Void> login = client.withGateway(this::initialiseGateway);
    login.block();
  }

  private Mono<Void> initialiseGateway(GatewayDiscordClient gateway) {

    // Final Fantasy XIV Topics Feed
    Schedulers.parallel()
        .schedulePeriodically(
            () -> xivHandler.runFFXIVTopicsTask(gateway).subscribe(), 0, 10, TimeUnit.MINUTES);

    // Final Fantasy XIV News Feed
    Schedulers.parallel()
        .schedulePeriodically(
            () -> xivHandler.runFFXIVNewsTask(gateway).subscribe(), 1, 10, TimeUnit.MINUTES);

    // Final Fantasy XI Topics Feed
    Schedulers.parallel()
        .schedulePeriodically(
            () -> xiHandler.runFFXITopicsTask(gateway).subscribe(), 0, 10, TimeUnit.MINUTES);

    // Final Fantasy XI Information Feed
    Schedulers.parallel()
        .schedulePeriodically(
            () -> xiHandler.runFFXIInformationTask(gateway).subscribe(), 1, 10, TimeUnit.MINUTES);

    // War Thunder News Feed
    Schedulers.parallel()
        .schedulePeriodically(
            () -> warThunderHandler.runWarThunderTask(gateway).subscribe(),
            0,
            10,
            TimeUnit.MINUTES);

    // World of Warcraft News Feed
    Schedulers.parallel()
        .schedulePeriodically(
            () -> worldOfWarcraftHandler.runNewsTask(gateway).subscribe(), 0, 10, TimeUnit.MINUTES);

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
