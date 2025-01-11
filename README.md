# Personal Project: MochiBot
## Discord Game News Bot
By [Sam Clark](https://github.com/SamC95)

MochiBot is a Java-based Discord Bot utilising [Discord4J](https://discord4j.com/), Google Cloud Firestore & [a custom-built web-scraper](https://github.com/SamC95/news-scraper)

The application makes use of the web-scraper so it is required to use this bot.

Some sensitive data is not included in the GitHub repo. These include:
* Google Cloud Firestore setup
* Discord Bot setup and token
* Channel Ids

## Contents
* [Project Aims](https://github.com/SamC95/mochi-bot?tab=readme-ov-file#project-aims)
* [Approach](https://github.com/SamC95/mochi-bot?tab=readme-ov-file#approach)
* [Technologies](https://github.com/SamC95/mochi-bot?tab=readme-ov-file#technologies)
* [Implementation](https://github.com/SamC95/mochi-bot?tab=readme-ov-file#implementation)
* [Key Learnings](https://github.com/SamC95/mochi-bot?tab=readme-ov-file#key-learnings)
* [Conclusions](https://github.com/SamC95/mochi-bot?tab=readme-ov-file#conclusions)

## Project Aims
* Develop a web-scraper application that parses Html data to retrieve posts
* Develop a discord bot that utilises the web-scraper application
* Bot should have functionality to check if the post is new or the same as the most recent post
* Firestore is utilised to store the most recent post data for each game
* Implements asynchronous design to ensure efficient retrieval of data
* Checks for new posts as specific intervals, duration of which is dependent on time of day
* Embeds new posts into discord channels with appropriate formatting
* Embedded posts include all necessary data (title, image, url, description)

## Approach

Prior to developing the application, I researched similar news bots to see what sort of functionality they provided and how they embedded posts. This was helpful in determining the key design goals I wanted to ensure I included in my application.
It also allowed me to pinpoint areas in which I wished to improve the design for my personal needs with the bot (i.e., more comprehensive with what is posted, rather than only specific posts)

I also had to research how to utilise Discord4J which involved looking over documentation and ensuring I understood the functionality that I needed, I opted to use Firestore due to its ease of use and the fact
that the relatively low amount of read/writes per day was well within Firestore's free limits, even if I were to add more games over time.

## Technologies

![Static Badge](https://img.shields.io/badge/java-white?style=for-the-badge&logoColor=white&color=%23FF7800)
![Static Badge](https://img.shields.io/badge/Discord4J-white?style=for-the-badge&logoColor=white&color=%235865F2)
![Static Badge](https://img.shields.io/badge/Firebase-white?style=for-the-badge&logo=Firebase&logoColor=white&color=%23DD2C00)
![Static Badge](https://img.shields.io/badge/Jsoup-white?style=for-the-badge&logoColor=white&color=%23000000)

## Implementation

The application makes use of a web scraper application that retrieves the most recent post from a specific page by comparing the most recent post, that is stored in a firestore document for each page that is being scraped, if the post data that is retrieved does not match the stored post then it will trigger the bot to post it onto the associated channel on the discord server. There are some downsides to this approach, particularly that the posts are dependent on a consistent approach on the page itself when it comes to posts. I opted to use Firestore during initial planning due to its ease of use but I may consider refactoring the application to utilise an SQL database so that I can store all posts that are retrieved over time and compare on if the post already exists in the database, rather than only comparing to the most recent post. This would prevent duplicate posts even if they aren't common.

```java
if (docSnapshot.exists()) {
      String currentTitle = docSnapshot.getString("title");
      String currentUrl = docSnapshot.getString("url");

      boolean isSameTitle = Objects.equals(currentTitle, post.getTitle());
      boolean isSameUrl = Objects.equals(currentUrl, post.getUrl());

      /*
      Both title and url are checked to avoid scenarios where the title may be updated due to typos or other reasons,
      which would cause a repost if it no longer matches the currently stored one.
      */
      if (isSameTitle || isSameUrl) {
        System.out.printf("[%s] [INFO] No new post for %s\n", LocalTime.now(), documentName);
        return null;
      }
      else {
        firestoreDocUpdater.updateDocumentWithPostData(docRef, post, documentName);
        return post;
      }
    }
    return null;
  }
```

When it comes to the implementation beyond that, the application scrapes each page at specific time intervals. The default is every 10 minutes, however this will change in specific cases; during the hours of 10PM - 6AM (Local Time) as new posts tend to be less frequent during these hours, the application will instead use intervals of 30 minutes to reduce load. The weekend days will also scan every 20 minutes during daytime hours (but night time interval still takes precedent over this) since posts on the weekends tend to be less common as well.

```java
private long setDynamicInterval(long defaultInterval) {
    LocalTime now = LocalTime.now();
    DayOfWeek day = LocalDate.now().getDayOfWeek();

    // Adjusts interval based on time/day to optimise load
    if (now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0))) {
      return defaultInterval + 20; // Longer interval during the nighttime hours.
    }
    else if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
      return defaultInterval + 10; // Longer interval on weekends
    }
    else {
      return defaultInterval;
    }
  }
```

The primary functionality of the application is performed through handler classes for each game that manage running the necessary tasks, such as retrieving post details and creating the embeds and posting onto discord, the rest of the functions are split into different categories. Any functionality that involves retrieving and scheduling the posts are performed in the RetrievePostDetails and PostScheduler classes. Whilst any functionality related to firestore, which includes comparing the new post with the most recently stored post; is handled in the firestore directory. Below is an example of a task running and embedding a post in one of the handler classes.

```java
public Mono<Void> runFFXIVNewsTask(GatewayDiscordClient gateway) {
    return Mono.fromRunnable(
        () -> {
          FFXIVHandler xivHandler = new FFXIVHandler();
          try {
            Update newsPost = xivHandler.FFXIVNewsHandler();
            if (newsPost != null) {
              getFFXIVUpdate(gateway, newsPost);
            }
          } catch (Exception e) {
              System.err.printf(
                      "[%s] [ERROR] Failed to fetch Final Fantasy XIV Lodestone news update: %s\n",
                      LocalTime.now(), e.getMessage());
          }
        });
  }

  private void getFFXIVUpdate(GatewayDiscordClient gateway, Update post) {
    var channelId = PropertiesLoader.loadProperties("FFXIV_CHANNEL_ID");
      String formattedDate = DateFormatter.getFormattedDate();

      gateway
        .getChannelById(Snowflake.of(channelId))
        .ofType(TextChannel.class)
        .flatMap(
            channel -> {
                String image =
                        post.getImage() != null && !Objects.equals(post.getImage(), "No image found")
                                ? post.getImage()
                                : "";

              EmbedCreateSpec embed =
                  EmbedCreateSpec.builder()
                      .author(post.getAuthor(), "https://eu.finalfantasyxiv.com/lodestone/", "")
                      .title(post.getTitle())
                      .url(post.getUrl())
                      .image(image)
                      .description(post.getDescription())
                      .thumbnail("https://lodestonenews.com/images/thumbnail.png")
                      .footer("News provided by MochiBot • " + formattedDate, "")
                      .build();
              return channel.createMessage(embed);
            })
        .subscribe();
  }

    @Override
    public Mono<Void> handleScheduledPost(GatewayDiscordClient gateway) {
        return runFFXIVTopicsTask(gateway).then(runFFXIVNewsTask(gateway));
    }
```

## Key Learnings
* Learnt to use Jsoup to scrape html data from a website and parse it effectively on the web scraper application
* Utilised Discord4J and learnt the basics of reactive programming in Java to utilise its library
* Gained knowledge of how to implement functionality to post to discord channels and create discord embeds
* Improved in providing effective logging of scheduler details, general information and handling of errors

## Conclusions

I learnt a good deal about reactive programming in Java, as well as Discord's API. These were particularly interesting topics and I would like to consider other projects that may utilise these in interesting ways. As this project was designed primarily for personal use, the program is not currently designed to be implemented on any server as the channel ids are hardcoded, this is something I may consider changing on this project but it was not needed for my use-case with the bot. 

I feel that utilising Firestore whilst convenient, did limit my design here a bit when it came to unforseen issues with repeated posts even if they are rare. I would definitely like to look at migrating the database aspects of the project onto SQL or something similar in the future. 

I have ensured that any scraping adheres to these websites rules based on robots.txt standards, and designed my application in such a way not to induce unreasonable load on these websites that could negatively impact them. 

