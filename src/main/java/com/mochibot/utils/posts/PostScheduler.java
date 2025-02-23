package com.mochibot.utils.posts;

import com.mochibot.data.FFXIHandler;
import com.mochibot.data.FFXIVHandler;
import com.mochibot.data.HellLetLooseHandler;
import com.mochibot.data.MHWildsHandler;
import com.mochibot.data.MarvelRivalsHandler;
import com.mochibot.data.OSRSHandler;
import com.mochibot.data.PathOfExile2Handler;
import com.mochibot.data.SatisfactoryGameHandler;
import com.mochibot.data.TheOldRepublicHandler;
import com.mochibot.data.ValheimHandler;
import com.mochibot.data.WarThunderHandler;
import com.mochibot.data.WorldOfWarcraftHandler;
import com.mochibot.utils.repository.mysql.DatabaseHandler;
import discord4j.core.GatewayDiscordClient;
import reactor.core.scheduler.Schedulers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PostScheduler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  DatabaseHandler databaseHandler = new DatabaseHandler();

  private final Map<GameHandler, Map<String, Long>> handlerScheduleMap =
      Map.ofEntries(
          Map.entry(
              new FFXIHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new FFXIVHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new HellLetLooseHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new MarvelRivalsHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new MHWildsHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new OSRSHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new PathOfExile2Handler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new ValheimHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new SatisfactoryGameHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new TheOldRepublicHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new WarThunderHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new WorldOfWarcraftHandler(retrievePostDetails, databaseHandler),
              Map.of("initialDelay", 0L, "interval", 10L)));

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
    DayOfWeek day = LocalDate.now().getDayOfWeek();

    // Adjusts interval based on time/day to optimise load
    if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0))) {
      return defaultInterval + 20; // Longer interval during the nighttime hours.
    } else if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
      return defaultInterval + 10; // Longer interval on weekends
    } else {
      return defaultInterval;
    }
  }
}
