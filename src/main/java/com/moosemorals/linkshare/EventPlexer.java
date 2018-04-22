package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

final class EventPlexer extends OffThreadHandler {
    private final static EventPlexer INSTANCE = new EventPlexer();
    private final Logger log = LoggerFactory.getLogger(EventPlexer.class);
    private final Set<PlexerListener> listeners = new HashSet<>();

    private EventPlexer() {

    }

    static EventPlexer getInstance() {
        return INSTANCE;
    }

    void addListener(PlexerListener l) {
        log.debug("Adding listener {}", l);
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    void removeListener(PlexerListener l) {
        log.debug("Removing listener {}", l);
        synchronized (listeners) {
            listeners.remove(l);
        }
    }


    void stop() {
        super.stop();
        synchronized (listeners) {
            for (PlexerListener l : listeners) {
                l.onShutdown();
            }
        }
    }

    void queueLink(Action action, Link link) {
        log.debug("Queuing link {}", link);
        super.addToQueue(new PlexerQueueItem(action, link));
    }

    protected void handleNext(QueueItem next) {
        final PlexerQueueItem item = (PlexerQueueItem) next;
        AuthManager auth = AuthManager.getInstance();
        final GCMBackend gcm = GCMBackend.getInstance();

        auth.forAllUsers(new AuthManager.EachUser() {
            @Override
            public void accept(User u) {
                if (!u.getPhones().isEmpty()) {
                    if (item.link.getTo().equals(u)) {
                        gcm.sendMessage(u, item.action, item.link);
                    }
                }
            }
        });

        synchronized (listeners) {
            log.debug("Sending to {} other(s)", listeners.size());
            for (PlexerListener l : listeners) {
                if (item.link.isRelated(l.getUser())) {
                    l.onItem(item);
                }
            }
        }
    }

    enum Action {
        CREATED, DELETED
    }

    interface PlexerListener {
        User getUser();

        void onItem(PlexerQueueItem item);

        void onShutdown();
    }

    final static class PlexerQueueItem extends OffThreadHandler.QueueItem {
        final EventPlexer.Action action;
        final Link link;

        PlexerQueueItem(EventPlexer.Action action, Link link) {
            this.action = action;
            this.link = link;
        }
    }
}