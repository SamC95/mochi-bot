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

import java.time.LocalTime;
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

  public void schedulePeriodicPosts(GatewayDiscordClient gateway) {
    handlerScheduleMap.forEach(
        (handler, scheduleMap) -> {
          long initialDelay = scheduleMap.get("initialDelay");
          long interval = scheduleMap.get("interval");

          Schedulers.parallel()
              .schedule(
                  () -> {
                    handler.handleScheduledPost(gateway).subscribe();

                    System.out.printf(
                        "[%s] [SCHEDULER] %s executed its first scheduled task\n",
                        LocalTime.now(), handler.getClass().getSimpleName());

                    // Dynamically schedule the next execution
                    dynamicScheduler(handler, gateway, interval);
                  },
                  initialDelay,
                  TimeUnit.MINUTES);
        });
  }

  private void dynamicScheduler(
      GameHandler handler, GatewayDiscordClient gateway, long defaultInterval) {
    long interval = setDynamicInterval(defaultInterval);

    System.out.printf(
        "[%s] [SCHEDULER] %s scheduled to run in %d minutes\n",
        LocalTime.now(), handler.getClass().getSimpleName(), interval);

    Schedulers.parallel()
        .schedule(
            () -> {
              handler.handleScheduledPost(gateway).subscribe();
              dynamicScheduler(handler, gateway, defaultInterval);
            },
            interval,
            TimeUnit.MINUTES);
  }

  private long setDynamicInterval(long defaultInterval) {
    LocalTime now = LocalTime.now();

    // Sets a longer interval during night hours to reduce load when new posts are less likely
    if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0))) {
      return defaultInterval + 20;
    }
    else {
      return defaultInterval;
    }
  }
}
