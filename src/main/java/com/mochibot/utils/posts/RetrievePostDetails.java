package com.mochibot.utils.posts;

import com.mochi.scraper.data.Borderlands4;
import com.mochi.scraper.data.CivilizationVII;
import com.mochi.scraper.data.FinalFantasyXI;
import com.mochi.scraper.data.FinalFantasyXIV;
import com.mochi.scraper.data.GenshinImpact;
import com.mochi.scraper.data.HellLetLoose;
import com.mochi.scraper.data.HonkaiStarRail;
import com.mochi.scraper.data.KillingFloor3;
import com.mochi.scraper.data.MarvelRivals;
import com.mochi.scraper.data.MonsterHunterWilds;
import com.mochi.scraper.data.Nikke;
import com.mochi.scraper.data.OldSchoolRuneScape;
import com.mochi.scraper.data.Overwatch;
import com.mochi.scraper.data.PathOfExile2;
import com.mochi.scraper.data.SatisfactoryGame;
import com.mochi.scraper.data.TheOldRepublic;
import com.mochi.scraper.data.Valheim;
import com.mochi.scraper.data.WarThunder;
import com.mochi.scraper.data.WorldOfWarcraft;
import com.mochi.scraper.data.WutheringWaves;
import com.mochi.scraper.data.ZenlessZoneZero;
import com.mochi.scraper.model.Update;
import com.mochi.scraper.utils.JsoupConnector;
import com.mochi.scraper.utils.PlaywrightConnector;
import com.mochi.scraper.utils.SteamRSSParser;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RetrievePostDetails {
  Borderlands4 borderlands4 = new Borderlands4(new JsoupConnector());
  CivilizationVII civilizationVII = new CivilizationVII(new JsoupConnector());
  FinalFantasyXIV finalFantasyXIV = new FinalFantasyXIV(new JsoupConnector());
  FinalFantasyXI finalFantasyXI = new FinalFantasyXI(new JsoupConnector());
  GenshinImpact genshinImpact = new GenshinImpact(new PlaywrightConnector());
  HellLetLoose hellLetLoose = new HellLetLoose(new JsoupConnector());
  HonkaiStarRail honkaiStarRail = new HonkaiStarRail(new PlaywrightConnector());
  KillingFloor3 killingFloor3 = new KillingFloor3(new JsoupConnector());
  MarvelRivals marvelRivals = new MarvelRivals(new JsoupConnector());
  MonsterHunterWilds monsterHunterWilds = new MonsterHunterWilds(new JsoupConnector());
  Nikke nikke = new Nikke(new PlaywrightConnector());
  OldSchoolRuneScape oldSchoolRuneScape = new OldSchoolRuneScape(new JsoupConnector());
  Overwatch overwatch = new Overwatch(new JsoupConnector());
  PathOfExile2 pathOfExile2 = new PathOfExile2(new JsoupConnector());
  SatisfactoryGame satisfactoryGame = new SatisfactoryGame(new JsoupConnector());
  TheOldRepublic theOldRepublic = new TheOldRepublic(new JsoupConnector());
  Valheim valheim = new Valheim(new JsoupConnector());
  WarThunder warThunder = new WarThunder(new JsoupConnector());
  WorldOfWarcraft worldOfWarcraft = new WorldOfWarcraft(new JsoupConnector());
  WutheringWaves wutheringWaves = new WutheringWaves(new PlaywrightConnector());
  ZenlessZoneZero zenlessZoneZero = new ZenlessZoneZero(new PlaywrightConnector());

  public Update getBorderlands4News() throws IOException {
    borderlands4.getNewsFeed();

    return borderlands4.newsFeed;
  }

  public Update getCivilizationVIINews() throws IOException {
    civilizationVII.getNewsFeed();

    return civilizationVII.newsFeed;
  }

  public Update getFinalFantasyXIVNews() throws IOException {
    finalFantasyXIV.getNewsFeed();

    return finalFantasyXIV.newsFeed;
  }

  public Update getFinalFantasyXIVTopics() throws IOException {
    finalFantasyXIV.getTopicFeed();

    return finalFantasyXIV.topicFeed;
  }

  public Update getFinalFantasyXITopics() throws IOException {
    finalFantasyXI.getTopicFeed();

    return finalFantasyXI.topicFeed;
  }

  public Update getFinalFantasyXIInformation() throws IOException {
    finalFantasyXI.getInformationFeed();

    return finalFantasyXI.informationFeed;
  }

  public Update getGenshinImpactNews() {
    CompletableFuture<Void> future =
        CompletableFuture.runAsync(
            () -> {
              try {
                genshinImpact.getNewsFeed();
              } catch (IOException e) {
                System.err.printf(
                    "[%s] [ERROR] Failed to retrieve genshin impact post: %s\n",
                    LocalTime.now(), e.getMessage());
              }
            });

    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to retrieve genshin impact post: %s\n",
          LocalTime.now(), e.getMessage());
    }

    return genshinImpact.newsFeed;
  }

  public Update getHellLetLooseNews() throws IOException {
    SteamRSSParser.getSteamRSSNewsFeed(
        "686810", hellLetLoose.newsFeed, hellLetLoose.jsoupConnector);

    return hellLetLoose.newsFeed;
  }

  public Update getHonkaiStarRailNews() {
    CompletableFuture<Void> future =
        CompletableFuture.runAsync(
            () -> {
              honkaiStarRail.getNewsFeed();
            });

    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to retrieve Honkai Star Rail post: %s\n",
          LocalTime.now(), e.getMessage());
    }

    return honkaiStarRail.newsFeed;
  }

  public Update getKillingFloor3News() throws IOException {
    SteamRSSParser.getSteamRSSNewsFeed(
        "1430190", killingFloor3.newsFeed, killingFloor3.jsoupConnector);

    return killingFloor3.newsFeed;
  }

  public Update getMarvelRivalsAnnouncements() throws IOException {
    marvelRivals.getFeed(
        "https://www.marvelrivals.com/announcements/", marvelRivals.announcementFeed);

    return marvelRivals.announcementFeed;
  }

  public Update getMarvelRivalsDevDiaries() throws IOException {
    marvelRivals.getFeed("https://www.marvelrivals.com/devdiaries/", marvelRivals.devDiaryFeed);

    return marvelRivals.devDiaryFeed;
  }

  public Update getMarvelRivalsUpdates() throws IOException {
    marvelRivals.getFeed("https://www.marvelrivals.com/gameupdate/", marvelRivals.updateFeed);

    return marvelRivals.updateFeed;
  }

  public Update getMarvelRivalsBalancePosts() throws IOException {
    marvelRivals.getFeed("https://www.marvelrivals.com/balancepost/", marvelRivals.balanceFeed);

    return marvelRivals.balanceFeed;
  }

  public Update getMonsterHunterWildsNews() throws IOException {
    SteamRSSParser.getSteamRSSNewsFeed(
        "2246340", monsterHunterWilds.newsFeed, monsterHunterWilds.jsoupConnector);

    return monsterHunterWilds.newsFeed;
  }

  public Update getNikkeNews() {
    CompletableFuture<Void> future =
        CompletableFuture.runAsync(
            () -> {
              nikke.getNewsFeed();
            });

    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to retrieve NIKKE post: %s\n", LocalTime.now(), e.getMessage());
    }

    return nikke.newsFeed;
  }

  public Update getOldSchoolRuneScapeNews() throws IOException {
    oldSchoolRuneScape.getNewsFeed();

    return oldSchoolRuneScape.newsFeed;
  }

  public Update getOverwatchNews() throws IOException {
    overwatch.getNewsFeed();

    return overwatch.newsFeed;
  }

  public Update getOverwatchPatchNotes() throws IOException {
    overwatch.getPatchNotes();

    return overwatch.patchFeed;
  }

  public Update getPathOfExile2News() throws IOException {
    SteamRSSParser.getSteamRSSNewsFeed(
        "2694490", pathOfExile2.newsFeed, pathOfExile2.jsoupConnector);

    return pathOfExile2.newsFeed;
  }

  public Update getPathOfExile2Hotfix() throws IOException {
    pathOfExile2.getPatchNotes();

    return pathOfExile2.patchFeed;
  }

  public Update getSatisfactoryGameNews() throws IOException {
    SteamRSSParser.getSteamRSSNewsFeed(
        "526870", satisfactoryGame.newsFeed, satisfactoryGame.jsoupConnector);

    return satisfactoryGame.newsFeed;
  }

  public Update getTheOldRepublicNews() throws IOException {
    theOldRepublic.getNewsFeed();

    return theOldRepublic.newsFeed;
  }

  public Update getValheimNews() throws IOException {
    valheim.getNewsFeed();

    return valheim.newsFeed;
  }

  public Update getWarThunderPinnedNews() throws IOException {
    warThunder.getPinnedNews();

    return warThunder.pinnedNewsFeed;
  }

  public Update getWarThunderUnpinnedNews() throws IOException {
    warThunder.getUnpinnedNews();

    return warThunder.unpinnedNewsFeed;
  }

  public Update getWarThunderPinnedChangelog() throws IOException {
    warThunder.getPinnedChangelog();

    return warThunder.pinnedChangelogFeed;
  }

  public Update getWarThunderUnpinnedChangelog() throws IOException {
    warThunder.getUnpinnedChangelog();

    return warThunder.unpinnedChangelogFeed;
  }

  public Update getWorldOfWarcraftNews() throws IOException {
    worldOfWarcraft.getNewsFeed();

    return worldOfWarcraft.newsFeed;
  }

  public Update getWutheringWavesNews() {
    CompletableFuture<Void> future =
        CompletableFuture.runAsync(
            () -> wutheringWaves.getNewsFeed());

    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to retrieve wuthering waves post: %s\n",
          LocalTime.now(), e.getMessage());
    }

    return wutheringWaves.newsFeed;
  }

  public Update getZenlessZoneZeroNews() {
    CompletableFuture<Void> future =
        CompletableFuture.runAsync(
            () -> {
              try {
                zenlessZoneZero.getNewsFeed();
              } catch (IOException e) {
                System.err.printf(
                    "[%s] [ERROR] Failed to retrieve zenless zone zero post: %s\n",
                    LocalTime.now(), e.getMessage());
              }
            });

    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      System.err.printf(
          "[%s] [ERROR] Failed to retrieve zenless zone zero post: %s\n",
          LocalTime.now(), e.getMessage());
    }

    return zenlessZoneZero.newsFeed;
  }
}
