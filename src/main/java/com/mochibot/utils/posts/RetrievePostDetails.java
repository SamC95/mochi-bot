package com.mochibot.utils.posts;

import com.example.scraper.data.FinalFantasyXI;
import com.example.scraper.data.FinalFantasyXIV;
import com.example.scraper.data.HellLetLoose;
import com.example.scraper.data.MarvelRivals;
import com.example.scraper.data.MonsterHunterWilds;
import com.example.scraper.data.OldSchoolRuneScape;
import com.example.scraper.data.Overwatch;
import com.example.scraper.data.PathOfExile2;
import com.example.scraper.data.SatisfactoryGame;
import com.example.scraper.data.TheOldRepublic;
import com.example.scraper.data.Valheim;
import com.example.scraper.data.WarThunder;
import com.example.scraper.data.WorldOfWarcraft;
import com.example.scraper.model.Update;
import com.example.scraper.utils.JsoupConnector;
import com.example.scraper.utils.SteamRSSParser;

import java.io.IOException;

public class RetrievePostDetails {
  FinalFantasyXIV finalFantasyXIV = new FinalFantasyXIV(new JsoupConnector());
  FinalFantasyXI finalFantasyXI = new FinalFantasyXI(new JsoupConnector());
  HellLetLoose hellLetLoose = new HellLetLoose(new JsoupConnector());
  MarvelRivals marvelRivals = new MarvelRivals(new JsoupConnector());
  MonsterHunterWilds monsterHunterWilds = new MonsterHunterWilds(new JsoupConnector());
  OldSchoolRuneScape oldSchoolRuneScape = new OldSchoolRuneScape(new JsoupConnector());
  Overwatch overwatch = new Overwatch(new JsoupConnector());
  PathOfExile2 pathOfExile2 = new PathOfExile2(new JsoupConnector());
  SatisfactoryGame satisfactoryGame = new SatisfactoryGame(new JsoupConnector());
  TheOldRepublic theOldRepublic = new TheOldRepublic(new JsoupConnector());
  Valheim valheim = new Valheim(new JsoupConnector());
  WarThunder warThunder = new WarThunder(new JsoupConnector());
  WorldOfWarcraft worldOfWarcraft = new WorldOfWarcraft(new JsoupConnector());

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

  public Update getHellLetLooseNews() throws IOException {
    SteamRSSParser.getSteamRSSNewsFeed(
        "686810", hellLetLoose.newsFeed, hellLetLoose.jsoupConnector);

    return hellLetLoose.newsFeed;
  }

  public Update getMarvelRivalsAnnouncements() throws IOException {
    marvelRivals.getFeed("https://www.marvelrivals.com/news/", marvelRivals.announcementFeed);

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

  public Update getMonsterHunterWildsNews() throws IOException {
    SteamRSSParser.getSteamRSSNewsFeed(
        "2246340", monsterHunterWilds.newsFeed, monsterHunterWilds.jsoupConnector);

    return monsterHunterWilds.newsFeed;
  }

  public Update getOldSchoolRuneScapeNews() throws IOException {
    oldSchoolRuneScape.getNewsFeed();

    return oldSchoolRuneScape.newsFeed;
  }

  public Update getOverwatchNews() throws IOException {
    overwatch.getNewsFeed();

    return overwatch.update;
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
}
