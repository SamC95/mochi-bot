package com.example.mochibot.utils.firestore;

import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreDocUpdater {

  public void updateDocumentWithPostData(DocumentReference docRef, Update post, String documentName)
      throws ExecutionException, InterruptedException {

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

    WriteResult result = docRef.set(newData).get();
    System.out.printf(
        "[%s} [INFO] New post has been stored and posted for %s\n",
        result.getUpdateTime().toDate(), documentName);
  }
}
