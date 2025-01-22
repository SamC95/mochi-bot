package com.example.mochibot.utils.posts;

import com.example.mochibot.data.FFXIHandler;
import com.example.mochibot.data.FFXIVHandler;
import com.example.mochibot.data.HellLetLooseHandler;
import com.example.mochibot.data.MHWildsHandler;
import com.example.mochibot.data.MarvelRivalsHandler;
import com.example.mochibot.data.OSRSHandler;
import com.example.mochibot.data.PathOfExile2Handler;
import com.example.mochibot.data.SatisfactoryGameHandler;
import com.example.mochibot.data.TheOldRepublicHandler;
import com.example.mochibot.data.WarThunderHandler;
import com.example.mochibot.data.WorldOfWarcraftHandler;
import com.example.mochibot.utils.repository.firestore.FirestoreBuilder;
import com.example.mochibot.utils.repository.firestore.FirestoreDocUpdater;
import discord4j.core.GatewayDiscordClient;
import reactor.core.scheduler.Schedulers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PostScheduler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();
  FirestoreBuilder firestoreBuilder = new FirestoreBuilder();

  private final Map<GameHandler, Map<String, Long>> handlerScheduleMap =
      Map.ofEntries(
          Map.entry(
              new FFXIHandler(retrievePostDetails, firestoreDocUpdater, firestoreBuilder),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new FFXIVHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new WarThunderHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new WorldOfWarcraftHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new HellLetLooseHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new TheOldRepublicHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new MHWildsHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new SatisfactoryGameHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new PathOfExile2Handler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 0L, "interval", 10L)),
          Map.entry(
              new OSRSHandler(retrievePostDetails, firestoreDocUpdater),
              Map.of("initialDelay", 5L, "interval", 10L)),
          Map.entry(
              new MarvelRivalsHandler(retrievePostDetails, firestoreDocUpdater),
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
