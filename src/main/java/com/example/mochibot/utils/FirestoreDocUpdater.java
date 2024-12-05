package com.example.mochibot.utils;

import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirestoreDocUpdater {

    public void updateDocumentWithPostData(DocumentReference docRef, Update topicsPost)
            throws ExecutionException, InterruptedException {
        Map<String, Object> newData = new HashMap<>();
        newData.put("title", topicsPost.getTitle());
        newData.put("author", topicsPost.getAuthor());
        newData.put("url", topicsPost.getUrl());
        newData.put("imageUrl", topicsPost.getImage());
        newData.put("description", topicsPost.getDescription());

        WriteResult result = docRef.set(newData).get();
        System.out.println("Document updated at: " + result.getUpdateTime().toDate());
    }
}
