package handlers;

import com.mochibot.data.FFXIHandler;
import com.mochibot.utils.posts.RetrievePostDetails;
import com.mochibot.utils.repository.firestore.FirestoreBuilder;
import com.mochibot.utils.repository.firestore.FirestoreDocUpdater;
import com.example.scraper.model.Update;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FFXIHandlerTest {

  private RetrievePostDetails mockRetrievePostDetails;
  private Firestore mockFirestore;
  private CollectionReference mockCollection;
  private DocumentReference mockDocRef;
  private DocumentSnapshot mockSnapshot;
  private FFXIHandler handler;

  @BeforeEach
  void setUp() throws ExecutionException, InterruptedException {
    mockRetrievePostDetails = mock(RetrievePostDetails.class);
    FirestoreBuilder mockFirestoreBuilder = mock(FirestoreBuilder.class);
    mockFirestore = mock(Firestore.class);
    mockCollection = mock(CollectionReference.class);
    mockDocRef = mock(DocumentReference.class);
    ApiFuture mockApiFuture = mock(ApiFuture.class);
    mockSnapshot = mock(DocumentSnapshot.class);
    FirestoreDocUpdater mockFirestoreDocUpdater = mock(FirestoreDocUpdater.class);

    when(mockFirestoreBuilder.getFirestore()).thenReturn(mockFirestore);
    when(mockFirestore.collection("games")).thenReturn(mockCollection);
    when(mockDocRef.get()).thenReturn(mockApiFuture);

    when(mockApiFuture.get()).thenReturn(mockSnapshot);

    when(mockSnapshot.exists()).thenReturn(true);

    handler =
        new FFXIHandler(mockRetrievePostDetails, mockFirestoreDocUpdater, mockFirestoreBuilder);
  }

  @Test
  public void testTopicsHandler() throws Exception {
    when(mockCollection.document("102")).thenReturn(mockDocRef);

    Update mockUpdate = new Update();
    mockUpdate.setTitle("Final Fantasy XI Title");
    mockUpdate.setDescription("Final Fantasy XI Description");
    mockUpdate.setUrl("Final Fantasy XI Url");

    when(mockRetrievePostDetails.getFinalFantasyXITopics()).thenReturn(mockUpdate);

    Method topicsHandlerMethod = FFXIHandler.class.getDeclaredMethod("topicsHandler");
    topicsHandlerMethod.setAccessible(true);

    Update result = (Update) topicsHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Final Fantasy XI Title", result.getTitle());
    Assertions.assertEquals("Final Fantasy XI Description", result.getDescription());
    Assertions.assertEquals("Final Fantasy XI Url", result.getUrl());

    verify(mockRetrievePostDetails, times(1)).getFinalFantasyXITopics();
    verify(mockFirestore, times(1)).collection("games");
    verify(mockCollection, times(1)).document("102");
    verify(mockDocRef, times(1)).get();
    verify(mockSnapshot, times(1)).exists();
  }

  @Test
  public void testInformationHandler() throws Exception {
    when(mockCollection.document("103")).thenReturn(mockDocRef);

    Update mockUpdate = new Update();
    mockUpdate.setTitle("Final Fantasy XI Information Title");
    mockUpdate.setDescription("Final Fantasy XI Information Description");
    mockUpdate.setUrl("Final Fantasy XI Url");

    when(mockRetrievePostDetails.getFinalFantasyXIInformation()).thenReturn(mockUpdate);

    Method informationHandlerMethod = FFXIHandler.class.getDeclaredMethod("informationHandler");
    informationHandlerMethod.setAccessible(true);

    Update result = (Update) informationHandlerMethod.invoke(handler);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("Final Fantasy XI Information Title", result.getTitle());
    Assertions.assertEquals("Final Fantasy XI Information Description", result.getDescription());
    Assertions.assertEquals("Final Fantasy XI Url", result.getUrl());

    verify(mockRetrievePostDetails, times(1)).getFinalFantasyXIInformation();
    verify(mockFirestore, times(1)).collection("games");
    verify(mockCollection, times(1)).document("103");
    verify(mockDocRef, times(1)).get();
    verify(mockSnapshot, times(1)).exists();
  }
}
