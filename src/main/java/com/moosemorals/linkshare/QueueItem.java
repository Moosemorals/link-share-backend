package com.moosemorals.linkshare;

final class QueueItem {
    final EventPlexer.Action action;
    final Link link;
    QueueItem(EventPlexer.Action action, Link link) {
        this.action = action;
        this.link = link;
    }
}
