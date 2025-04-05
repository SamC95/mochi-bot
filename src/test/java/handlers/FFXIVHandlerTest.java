package handlers;

import com.mochi.scraper.model.Update;
import com.mochibot.handlers.FFXIVHandler;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.mochibot.utils.repository.mysql.DatabaseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FFXIVHandlerTest {
  private RetrievePostDetails mockRetrievePostDetails;
  private DatabaseHandler mockDatabaseHandler;
  private FFXIVHandler handler;

  @BeforeEach
  void setUp() {
    mockRetrievePostDetails = mock(RetrievePostDetails.class);
    mockDatabaseHandler = mock(DatabaseHandler.class);

    handler = new FFXIVHandler(mockRetrievePostDetails, mockDatabaseHandler);
  }

  @Test
  public void newsHandlerShouldReturnPostData() throws Exception {
    Update mockUpdate = new Update();

    mockUpdate.setTitle("Final Fantasy XIV News Title");
    mockUpdate.setAuthor("Final Fantasy XIV News Author");
    mockUpdate.setUrl("Final Fantasy XIV News Url");
    mockUpdate.setDescription("Final Fantasy XIV News Description");

    when(mockRetrievePostDetails.getFinalFantasyXIVNews()).thenReturn(mockUpdate);
    when(mockDatabaseHandler.getUpdate(mockUpdate, "Final Fantasy XIV news", 101))
        .thenReturn(mockUpdate);

    Method newsHandlerMethod = FFXIVHandler.class.getDeclaredMethod("newsHandler");
    newsHandlerMethod.setAccessible(true);

    Update result = (Update) newsHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Final Fantasy XIV News Title", result.getTitle());
    Assertions.assertEquals("Final Fantasy XIV News Author", result.getAuthor());
    Assertions.assertEquals("Final Fantasy XIV News Url", result.getUrl());
    Assertions.assertEquals("Final Fantasy XIV News Description", result.getDescription());

    verify(mockRetrievePostDetails, Mockito.times(1)).getFinalFantasyXIVNews();
    verify(mockDatabaseHandler, Mockito.times(1))
        .getUpdate(mockUpdate, "Final Fantasy XIV news", 101);
  }

  @Test
  public void topicsHandlerShouldReturnPostData() throws Exception {
    Update mockUpdate = new Update();

    mockUpdate.setTitle("Final Fantasy XIV Topics Title");
    mockUpdate.setAuthor("Final Fantasy XIV Topics Author");
    mockUpdate.setUrl("Final Fantasy XIV Topics Url");
    mockUpdate.setDescription("Final Fantasy XIV Topics Description");
    mockUpdate.setImage("Final Fantasy XIV Topics Image Url");

    when(mockRetrievePostDetails.getFinalFantasyXIVTopics()).thenReturn(mockUpdate);
    when(mockDatabaseHandler.getUpdate(mockUpdate, "Final Fantasy XIV topics", 100))
        .thenReturn(mockUpdate);

    Method topicsHandlerMethod = FFXIVHandler.class.getDeclaredMethod("topicsHandler");
    topicsHandlerMethod.setAccessible(true);

    Update result = (Update) topicsHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Final Fantasy XIV Topics Title", result.getTitle());
    Assertions.assertEquals("Final Fantasy XIV Topics Author", result.getAuthor());
    Assertions.assertEquals("Final Fantasy XIV Topics Url", result.getUrl());
    Assertions.assertEquals("Final Fantasy XIV Topics Description", result.getDescription());
    Assertions.assertEquals("Final Fantasy XIV Topics Image Url", result.getImage());

    verify(mockRetrievePostDetails, Mockito.times(1)).getFinalFantasyXIVTopics();
    verify(mockDatabaseHandler, Mockito.times(1))
        .getUpdate(mockUpdate, "Final Fantasy XIV topics", 100);
  }
}
