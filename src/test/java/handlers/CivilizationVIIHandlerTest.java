package handlers;

import com.mochi.scraper.model.Update;
import com.mochibot.handlers.CivilizationVIIHandler;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.mochibot.utils.repository.mysql.DatabaseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class CivilizationVIIHandlerTest {
  private RetrievePostDetails mockRetrievePostDetails;
  private DatabaseHandler mockDatabaseHandler;
  private CivilizationVIIHandler handler;

  @BeforeEach
  void setUp() {
    mockRetrievePostDetails = mock(RetrievePostDetails.class);
    mockDatabaseHandler = mock(DatabaseHandler.class);

    handler = new CivilizationVIIHandler(mockRetrievePostDetails, mockDatabaseHandler);
  }

  @Test
  public void newsHandlerShouldReturnPostData() throws Exception {
    Update mockUpdate = new Update();

    mockUpdate.setTitle("Civilization VII News Title");
    mockUpdate.setDescription("Civilization VII News Description");
    mockUpdate.setUrl("Civilization VII News URL");
    mockUpdate.setImage("Civilization VII News Image URL");

    when(mockRetrievePostDetails.getCivilizationVIINews()).thenReturn(mockUpdate);
    when(mockDatabaseHandler.getUpdate(mockUpdate, "Sid Meier's Civilization VII", 122))
        .thenReturn(mockUpdate);

    Method newsHandlerMethod = CivilizationVIIHandler.class.getDeclaredMethod("newsHandler");
    newsHandlerMethod.setAccessible(true);

    Update result = (Update) newsHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Civilization VII News Title", result.getTitle());
    Assertions.assertEquals("Civilization VII News Description", result.getDescription());
    Assertions.assertEquals("Civilization VII News URL", result.getUrl());
    Assertions.assertEquals("Civilization VII News Image URL", result.getImage());

    Mockito.verify(mockRetrievePostDetails, times(1)).getCivilizationVIINews();
    Mockito.verify(mockDatabaseHandler, times(1))
        .getUpdate(mockUpdate, "Sid Meier's Civilization VII", 122);
  }
}
