package com.moosemorals.linkshare;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class OffThreadHandler {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<QueueItem> queue = new LinkedList<>();
    private final Runnable queueHandler = new Runnable() {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                QueueItem next;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    next = queue.remove(0);
                }
                handleNext(next);
            }
        }
    };

    private Thread queueThread;

    void start() {
        if (running.compareAndSet(false, true)) {
            queueThread = new Thread(queueHandler, "QueueHandler");
            queueThread.start();
        }
    }

    void stop() {
        if (running.compareAndSet(true, false)) {
            queueThread.interrupt();
        }
    }

    void addToQueue(QueueItem next) {
        synchronized (queue) {
            queue.add(next);
            queue.notifyAll();
        }
    }

    protected abstract void handleNext(QueueItem next);

    abstract static class QueueItem {}
}
