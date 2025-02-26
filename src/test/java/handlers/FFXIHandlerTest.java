package handlers;

import com.mochibot.handlers.FFXIHandler;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.example.scraper.model.Update;
import com.mochibot.utils.repository.mysql.DatabaseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FFXIHandlerTest {
  private RetrievePostDetails mockRetrievePostDetails;
  private DatabaseHandler mockDatabaseHandler;
  private FFXIHandler handler;

  @BeforeEach
  void setUp() {
    mockRetrievePostDetails = mock(RetrievePostDetails.class);
    mockDatabaseHandler = mock(DatabaseHandler.class);

    handler = new FFXIHandler(mockRetrievePostDetails, mockDatabaseHandler);
  }

  @Test
  public void testTopicsHandler() throws Exception {
    Update mockUpdate = new Update();
    mockUpdate.setTitle("Final Fantasy XI Topics Title");
    mockUpdate.setDescription("Final Fantasy XI Topics Description");
    mockUpdate.setUrl("Final Fantasy XI Topics Url");

    when(mockRetrievePostDetails.getFinalFantasyXITopics()).thenReturn(mockUpdate);
    when(mockDatabaseHandler.getUpdate(mockUpdate, "Final Fantasy XI topics", 102))
        .thenReturn(mockUpdate);

    Method topicsHandlerMethod = FFXIHandler.class.getDeclaredMethod("topicsHandler");
    topicsHandlerMethod.setAccessible(true);

    Update result = (Update) topicsHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Final Fantasy XI Topics Title", result.getTitle());
    Assertions.assertEquals("Final Fantasy XI Topics Description", result.getDescription());
    Assertions.assertEquals("Final Fantasy XI Topics Url", result.getUrl());

    verify(mockRetrievePostDetails, times(1)).getFinalFantasyXITopics();
    verify(mockDatabaseHandler, times(1)).getUpdate(mockUpdate, "Final Fantasy XI topics", 102);
  }

  @Test
  public void testInformationHandler() throws Exception {
    Update mockUpdate = new Update();
    mockUpdate.setTitle("Final Fantasy XI Information Title");
    mockUpdate.setDescription("Final Fantasy XI Information Description");
    mockUpdate.setUrl("Final Fantasy XI Information Url");

    when(mockRetrievePostDetails.getFinalFantasyXIInformation()).thenReturn(mockUpdate);
    when(mockDatabaseHandler.getUpdate(mockUpdate, "Final Fantasy XI information", 103))
        .thenReturn(mockUpdate);

    Method informationHandlerMethod = FFXIHandler.class.getDeclaredMethod("informationHandler");
    informationHandlerMethod.setAccessible(true);

    Update result = (Update) informationHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Final Fantasy XI Information Title", result.getTitle());
    Assertions.assertEquals("Final Fantasy XI Information Description", result.getDescription());
    Assertions.assertEquals("Final Fantasy XI Information Url", result.getUrl());

    verify(mockRetrievePostDetails, times(1)).getFinalFantasyXIInformation();
    verify(mockDatabaseHandler, times(1))
        .getUpdate(mockUpdate, "Final Fantasy XI information", 103);
  }
}
