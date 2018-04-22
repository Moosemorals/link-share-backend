package com.moosemorals.linkshare;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

final class GCMBackend {

    private static final GCMBackend INSTANCE = new GCMBackend();

    private static final String ACCOUNT_KEY_PATH = "/home/osric/links/googleKey.json";
    private final Logger log = LoggerFactory.getLogger(GCMBackend.class);
    private final SingleThreadExecutor executor = new SingleThreadExecutor();

    private GCMBackend() {
        init();
    }

    static GCMBackend getInstance() {
        return INSTANCE;
    }

    private void init() {
        try (FileInputStream serviceAccount = new FileInputStream(ACCOUNT_KEY_PATH)) {

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://linkshare-6351a.firebaseio.com/")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException ex) {
            log.error("Can't auth google", ex);
        }
    }

    void sendMessage(User user, EventPlexer.Action action, Link link) {
        for (String phone : user.getPhones()) {
            Message msg = Message.builder()
                    .setToken(phone)
                    .putData("action", action.toString())
                    .putData("link", link.toJson().toString())
                    .build();

            final ApiFuture<String> async = FirebaseMessaging.getInstance().sendAsync(msg);
            async.addListener(new Runnable() {
                @Override
                public void run() {
                    String response ;
                    try {
                        response = async.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.debug("Can't get response", e);
                        return;
                    }
                    log.debug("Sent message, got response {}", response);
                }
            }, executor);
        }
    }
}
