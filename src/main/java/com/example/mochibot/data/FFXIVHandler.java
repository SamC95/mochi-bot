package com.example.mochibot.data;

import com.example.mochibot.utils.FirestoreDocUpdater;
import com.example.mochibot.utils.RetrievePostDetails;
import com.example.scraper.model.Update;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class FFXIVHandler {
  RetrievePostDetails retrievePostDetails = new RetrievePostDetails();
  FirestoreDocUpdater firestoreDocUpdater = new FirestoreDocUpdater();

  public Update FFXIVTopicsHandler() throws IOException, ExecutionException, InterruptedException {
    Update topicsPost = retrievePostDetails.getFinalFantasyXIVTopics();

    Firestore database = FirestoreClient.getFirestore();

    DocumentReference docRef = database.collection("games").document("100");

    DocumentSnapshot docSnapshot = docRef.get().get();

    if (docSnapshot.exists()) {
      String currentTitle = docSnapshot.getString("title");

      if (currentTitle != null && currentTitle.equals(topicsPost.getTitle())) {
        System.out.println("Title matches current firestore title");
        return null;
      }
      else {
        firestoreDocUpdater.updateDocumentWithPostData(docRef, topicsPost);
        return topicsPost;
      }
    }
      return null;
  }
}
