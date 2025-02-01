package com.mochibot.utils.repository.firestore;

import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class FirestoreDocUpdater {
  public void updateDocumentWithPostData(
      DocumentReference docRef, Update post, String documentName) {

    if (post == null || post.getTitle() == null || post.getUrl() == null) {
      System.err.printf(
          "[%s] [ERROR] Post data for %s is invalid or missing, skipping update.\n",
          LocalTime.now(), documentName);
      return;
    }

    Map<String, Object> newData = new HashMap<>();

    newData.put("title", post.getTitle());
    newData.put("author", post.getAuthor());
    newData.put("url", post.getUrl());
    newData.put("imageUrl", post.getImage());
    newData.put("description", post.getDescription());

    docRef.set(newData);
    System.out.printf(
        "[%s} [INFO] New post has been stored and posted for %s\n", LocalTime.now(), documentName);
  }
}
