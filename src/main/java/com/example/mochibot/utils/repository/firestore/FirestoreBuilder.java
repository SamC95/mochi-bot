package com.example.mochibot.utils.repository.firestore;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FirestoreBuilder {
    public void setUpFirebase() throws IOException {
        // File not included on GitHub repo
        FileInputStream serviceAccount = new FileInputStream("src/main/resources/mochi-bot.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);

        FirestoreClient.getFirestore();
    }
}
