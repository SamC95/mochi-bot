package com.example.mochibot.utils.firestore;

import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;

import java.time.Instant;
import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

public class UpdateHandler {
  public static Update getUpdate(
          Update post, DocumentReference docRef, FirestoreDocUpdater firestoreDocUpdater, String documentName)
      throws InterruptedException, ExecutionException {
    DocumentSnapshot docSnapshot = docRef.get().get();

    if (docSnapshot.exists()) {
      String currentTitle = docSnapshot.getString("title");

      if (currentTitle != null && currentTitle.equals(post.getTitle())) {
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
}
