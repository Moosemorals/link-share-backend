package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public final class Backend extends HttpServlet {

    private final long TIMEOUT = 8 * 60 * 1000;     // 8 minutes
    private final Logger log = LoggerFactory.getLogger(Backend.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        final User user = (User) req.getAttribute(AuthFilter.USER);

        final Thread servletThread = Thread.currentThread();
        final LinkedList<EventPlexer.PlexerQueueItem> queue = new LinkedList<>();
        final EventPlexer.PlexerListener listener = new EventPlexer.PlexerListener() {
            @Override
            public User getUser() {
                return user;
            }

            @Override
            public void onItem(EventPlexer.PlexerQueueItem item) {
                synchronized (queue) {
                    queue.addLast(item);
                    queue.notifyAll();
                }
            }

            @Override
            public void onShutdown() {
                servletThread.interrupt();
            }
        };

        log.info("{}: Connected", req.getRemoteAddr());

        EventPlexer.getInstance().addListener(listener);

        resp.setHeader("Cache-Control", "no-cache");
        resp.setContentType("text/event-stream;charset=utf-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Connection", "close");
        try (PrintWriter out = resp.getWriter()) {
            write(out, ":\n\n");

            while (!Thread.interrupted()) {
                try {
                    EventPlexer.PlexerQueueItem next;
                    synchronized (queue) {
                        while (queue.isEmpty()) {
                            queue.wait(TIMEOUT);
                        }

                        if (!queue.isEmpty()) {
                            next = queue.removeFirst();
                        } else {
                            next = null;
                        }
                    }

                    if (next == null) {
                        // was a timeout, so just send a keep alive
                        log.debug("Sending a keep alive");
                        write(out, ":\n\n");
                        continue;
                    }

                    String event = buildEvent(next);
                    log.debug("{}: Sending event: {}", req.getRemoteAddr(), event);
                    write(out, event);

                    if (out.checkError()) {
                        log.info("{}: Remote end closed connection", req.getRemoteAddr());
                        break;
                    }
                } catch (InterruptedException e) {
                    log.info("Closing down");
                    write(out, "close: closed\n\n");
                    out.close();
                    break;
                }
            }
        } finally {
            EventPlexer.getInstance().removeListener(listener);
            log.debug("{}: No longer connected", req.getRemoteAddr());
        }

    }

    private String buildEvent(EventPlexer.PlexerQueueItem item) {
        return "event: " +
                item.action.toString().toLowerCase() +
                "\ndata: " +
                item.link.toJson().toString() +
                "\n\n";

    }

    private void write(PrintWriter out, String msg) throws IOException {
        out.print(msg);
        if (out.checkError()) {
            throw new IOException("Print error (write)");
        }
        out.flush();
        if (out.checkError()) {
            throw new IOException("Print error (flush)");
        }
    }
}