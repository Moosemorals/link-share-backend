package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

final class EventPlexer {
    private final Logger log = LoggerFactory.getLogger(EventPlexer.class);
    private final static EventPlexer INSTANCE = new EventPlexer();

    static EventPlexer getInstance() {
        return INSTANCE;
    }

    private final LinkedList<QueueItem> queue;
    private final Set<PlexerListener> listeners = new HashSet<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Runnable queueWatcher = new Runnable() {
        @Override
        public void run() {
            while (running.get() && !Thread.interrupted()) {
                QueueItem next;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }
                    next = queue.removeFirst();
                }

                notifyListeners(next);
            }
        }
    };
    private Thread watcherThread;

    private EventPlexer() {
        queue = new LinkedList<>();
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

    void start() {
        if (running.compareAndSet(false, true)) {
            watcherThread = new Thread(queueWatcher, "watcher");
            watcherThread.start();
        }
    }

    void stop() {
        if (running.compareAndSet(true, false)) {
            watcherThread.interrupt();
            watcherThread = null;

            synchronized (listeners) {
                for (PlexerListener l : listeners) {
                    l.onShutdown();
                }
            }
        }
    }

    void queueLink(Action action, Link link) {
        log.debug("Queuing link {}", link);
        synchronized (queue) {
            queue.addLast(new QueueItem(action, link));
            queue.notifyAll();
        }
    }

    private void notifyListeners(QueueItem item) {
        synchronized (listeners) {
            log.debug("Sending to {} other(s)", listeners.size());
            for (PlexerListener l : listeners) {
                if (l.getUser().equals(item.link.getTo())) {
                    l.onNewLink(item);
                }
            }
        }
    }

    interface PlexerListener {
        User getUser();
        void onNewLink(QueueItem item);
        void onShutdown();
    }

    enum Action {
        CREATED, DELETED
    }
}