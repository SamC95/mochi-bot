package com.example.mochibot.utils.posts;

import com.example.mochibot.data.FFXIHandler;
import com.example.mochibot.data.FFXIVHandler;
import com.example.mochibot.data.HellLetLooseHandler;
import com.example.mochibot.data.MHWildsHandler;
import com.example.mochibot.data.OSRSHandler;
import com.example.mochibot.data.PathOfExile2Handler;
import com.example.mochibot.data.SatisfactoryGameHandler;
import com.example.mochibot.data.TheOldRepublicHandler;
import com.example.mochibot.data.WarThunderHandler;
import com.example.mochibot.data.WorldOfWarcraftHandler;
import discord4j.core.GatewayDiscordClient;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PostScheduler {
  private final Map<GameHandler, Map<String, Long>> handlerScheduleMap =
      Map.of(
          new FFXIHandler(), Map.of("initialDelay", 0L, "interval", 10L),
          new FFXIVHandler(), Map.of("initialDelay", 0L, "interval", 10L),
          new WarThunderHandler(), Map.of("initialDelay", 0L, "interval", 10L),
          new WorldOfWarcraftHandler(), Map.of("initialDelay", 0L, "interval", 10L),
          new HellLetLooseHandler(), Map.of("initialDelay", 0L, "interval", 10L),
          new TheOldRepublicHandler(), Map.of("initialDelay", 5L, "interval", 10L),
          new MHWildsHandler(), Map.of("initialDelay", 5L, "interval", 10L),
          new SatisfactoryGameHandler(), Map.of("initialDelay", 5L, "interval", 10L),
          new PathOfExile2Handler(), Map.of("initialDelay", 5L, "interval", 10L),
          new OSRSHandler(), Map.of("initialDelay", 5L, "interval", 10L));

  private void scheduleHandler(
      GameHandler handler,
      GatewayDiscordClient gateway,
      long initialDelay,
      long interval) {

    Schedulers.parallel()
        .schedulePeriodically(
            () -> handler.handleScheduledPost(gateway).subscribe(), initialDelay, interval, TimeUnit.MINUTES);
  }

  public void schedulePeriodicPosts(GatewayDiscordClient gateway) {
    handlerScheduleMap.forEach(
        (handler, scheduleMap) -> {
          long initialDelay = scheduleMap.get("initialDelay");
          long interval = scheduleMap.get("interval");
            scheduleHandler(handler, gateway, initialDelay, interval);
        });
  }
}
