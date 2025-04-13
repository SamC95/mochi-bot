# Personal Project: MochiBot
## Discord Game News Bot
By [Sam Clark](https://github.com/SamC95)

MochiBot is a Java-based Discord Bot utilising [Discord4J](https://discord4j.com/), MySQL & [a custom-built web-scraper](https://github.com/SamC95/news-scraper)

The application makes use of the web-scraper application so it is required to use this bot.

Some sensitive data is not included in the GitHub repo. These include:
* MySQL Database setup
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
* MySQL is used to store post data and check if retrieved posts already exist in the database
* Implements asynchronous design to ensure efficient retrieval of data
* Checks for new posts as specific intervals, duration of which is dependent on time of day
* Embeds new posts into discord channels with appropriate formatting
* Embedded posts include all necessary data (title, image, url, description)

## Approach

Prior to developing the application, I researched similar news bots to see what sort of functionality they provided and how they embedded posts. This was helpful in determining the key design goals I wanted to ensure I included in my application.
It also allowed me to pinpoint areas in which I wished to improve the design for my personal needs with the bot (i.e., more comprehensive with what is posted, rather than only specific posts)

I also had to research how to utilise Discord4J which involved looking over documentation and ensuring I understood the functionality that I needed, I opted to use Firestore initially due to its ease of use and the fact that the relatively low amount of read/writes per day was well within Firestore's free limits, even if I were to add more games over time. However, as I continued to develop the application; I felt that Firestore was not the best fit for my design goals and instead opted to migrate database functionality to MySQL that allowed a more robust approach and removed issues with repeated posts or inconsistent news feed layouts.

## Technologies

**Current:**

![Static Badge](https://img.shields.io/badge/java-white?style=for-the-badge&logoColor=white&color=%23FF7800)<br>
![Static Badge](https://img.shields.io/badge/Discord4J-white?style=for-the-badge&logoColor=white&color=%235865F2)<br>
![Static Badge](https://img.shields.io/badge/mysql-white?style=for-the-badge&logo=MySQL&logoColor=white&color=%234479A1)<br>
![Static Badge](https://img.shields.io/badge/Jsoup-white?style=for-the-badge&logoColor=white&color=%23000000)<br>
![Static Badge](https://img.shields.io/badge/Playwright-white?style=for-the-badge&logoColor=white&color=green)<br>

**Previous:**

![Static Badge](https://img.shields.io/badge/Firebase-white?style=for-the-badge&logo=Firebase&logoColor=white&color=%23DD2C00)

## Implementation

The application makes use of a web scraper application that retrieves the most recent post from a specific page, and compares that post to previously stored posts in a database. If a post exists in the database with a matching title or URL, then the program will simply do nothing with the post it retrieved and will then be scheduled to recheck the page in a specified amount of time. If the post does not exist in the database then it will be returned to the handler class for that game and be posted onto the appropriate discord channel. Below is the code of the program checking for a match in the database. The program performs an automatic check every 24 hours to see if any posts in the database exceed six months since being stored, if so then they are removed.

```java
public Update getUpdate(Update post, String gameTitle, int gameId) throws SQLException {
    if (post == null || post.getTitle() == null || post.getUrl() == null) {
      System.err.printf(
              "[%s] [ERROR] Post data for %s is invalid or missing, skipping update.\n",
              LocalTime.now(), gameTitle);

      return null;
    }

    String query = "SELECT title, url FROM posts WHERE title = ? OR url = ?";

    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, post.getTitle());
      statement.setString(2, post.getUrl());

      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        String currentTitle = resultSet.getString("title");
        String currentUrl = resultSet.getString("url");

        boolean isSameTitle = Objects.equals(currentTitle, post.getTitle());
        boolean isSameUrl = Objects.equals(currentUrl, post.getUrl());

        if (isSameTitle || isSameUrl) {
          System.out.printf("[%s] [INFO] No new post for %s\n", LocalTime.now(), gameTitle);
        }
      } else {
        insertPost(post, gameId, gameTitle);
        return post;
      }
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

The primary functionality of the application is performed through handler classes for each game that manage running the necessary tasks, such as retrieving post details and creating the embeds and posting onto discord, the rest of the functions are split into different categories. Any functionality that involves retrieving and scheduling the posts are performed in the RetrievePostDetails and PostScheduler classes. Whilst any functionality related to database read/writes, which includes comparing the new post with the most recently stored post; is handled in the MySQL directory. Below is an example of a task running and embedding a post in one of the handler classes.

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
* Firestore being used initially improved my knowledge of its functionality, however it also allowed me to realise that MySQL was a better fit for my needs.
* Gained knowledge of how to implement functionality to post to discord channels and create discord embeds
* Improved in providing effective logging of scheduler details, general information and handling of errors

## Conclusions

I learnt a good deal about reactive programming in Java, as well as Discord's API. These were particularly interesting topics and I would like to consider other projects that may utilise these in interesting ways. As this project was designed primarily for personal use, the program is not currently designed to be implemented on any server as the channel ids are hardcoded, this is something I may consider changing on this project but it was not needed for my use-case with the bot. 

I feel that utilising Firestore whilst convenient, did limit my design here a bit when it came to unforseen issues with repeated posts due to inconsistent website news feeds. Migrating to MySQL resolved a lot of the issues I had and I felt that it was a valuable learning experience in ensuring that I do the proper research beforehand in terms of the technology stack that I opt to utilise and weighing up the pros and cons more thoroughly.

I have ensured that any scraping adheres to these websites rules based on robots.txt standards, and designed my application in such a way not to induce unreasonable load on these websites that could negatively impact them. 

