package handlers;

import com.mochi.scraper.model.Update;
import com.mochibot.handlers.GenshinImpactHandler;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.mochibot.utils.repository.mysql.DatabaseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GenshinImpactHandlerTest {
  private RetrievePostDetails mockRetrievePostDetails;
  private DatabaseHandler mockDatabaseHandler;
  private GenshinImpactHandler handler;

  @BeforeEach
  void setUp() {
    mockRetrievePostDetails = mock(RetrievePostDetails.class);
    mockDatabaseHandler = mock(DatabaseHandler.class);

    handler = new GenshinImpactHandler(mockRetrievePostDetails, mockDatabaseHandler);
  }

  @Test
  public void newsHandlerShouldReturnPostData() throws Exception {
    Update mockUpdate = new Update();

    mockUpdate.setTitle("Genshin Impact News Title");
    mockUpdate.setUrl("Genshin Impact News Url");
    mockUpdate.setImage("Genshin Impact News Image");
    mockUpdate.setDescription("Genshin Impact News Description");

    when(mockRetrievePostDetails.getGenshinImpactNews()).thenReturn(mockUpdate);
    when(mockDatabaseHandler.getUpdate(mockUpdate, "Genshin Impact", 124)).thenReturn(mockUpdate);

    Method newsHandlerMethod = GenshinImpactHandler.class.getDeclaredMethod("newsHandler");
    newsHandlerMethod.setAccessible(true);

    Update result = (Update) newsHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Genshin Impact News Title", result.getTitle());
    Assertions.assertEquals("Genshin Impact News Url", result.getUrl());
    Assertions.assertEquals("Genshin Impact News Image", result.getImage());
    Assertions.assertEquals("Genshin Impact News Description", result.getDescription());

    verify(mockRetrievePostDetails, times(1)).getGenshinImpactNews();
    verify(mockDatabaseHandler, times(1)).getUpdate(mockUpdate, "Genshin Impact", 124);
  }
}
