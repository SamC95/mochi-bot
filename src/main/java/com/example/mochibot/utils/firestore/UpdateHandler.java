package com.example.mochibot.utils.firestore;

import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;

import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class UpdateHandler {
  public static Update getUpdate(
          Update post, DocumentReference docRef, FirestoreDocUpdater firestoreDocUpdater, String documentName)
      throws InterruptedException, ExecutionException {
    DocumentSnapshot docSnapshot = docRef.get().get();

    if (docSnapshot.exists()) {
      String currentTitle = docSnapshot.getString("title");
      String currentUrl = docSnapshot.getString("url");

      /*
      Both title and url are checked to avoid scenarios where the title may be updated due to typos or other reasons,
      which would cause a repost if it no longer matches the currently stored one.
      */
      boolean isSameTitle = Objects.equals(currentTitle, post.getTitle());
      boolean isSameUrl = Objects.equals(currentUrl, post.getUrl());

      if (isSameTitle && isSameUrl) {
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
