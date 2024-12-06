package com.example.mochibot.utils;

import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

public class UpdateHandler {
  public static Update getUpdate(
      Update post, DocumentReference docRef, FirestoreDocUpdater firestoreDocUpdater)
      throws InterruptedException, ExecutionException {
    DocumentSnapshot docSnapshot = docRef.get().get();

    if (docSnapshot.exists()) {
      String currentTitle = docSnapshot.getString("title");

      if (currentTitle != null && currentTitle.equals(post.getTitle())) {
        System.out.println("Checked document at: " + Instant.now());
        return null;
      } else {
        firestoreDocUpdater.updateDocumentWithPostData(docRef, post);
        return post;
      }
    }
    return null;
  }
}
