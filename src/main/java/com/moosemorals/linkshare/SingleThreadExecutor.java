package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class SingleThreadExecutor extends OffThreadHandler implements Executor {
    private final Logger log = LoggerFactory.getLogger(SingleThreadExecutor.class);

    @Override
    public void execute(Runnable runnable) {
        addToQueue(new ExecutorQueueItem(runnable));
    }

    @Override
    protected void handleNext(QueueItem next) {
        try {
            ((ExecutorQueueItem) next).runnable.run();
        } catch (Exception ex) {
            log.error("Problem running executable", ex);
        }
    }

    private static class ExecutorQueueItem extends QueueItem {
        private Runnable runnable;

        private ExecutorQueueItem(Runnable runnable) {
            this.runnable = runnable;
        }
    }
}
