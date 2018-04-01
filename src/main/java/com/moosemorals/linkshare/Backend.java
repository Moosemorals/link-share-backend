package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

@WebServlet("/backend")
public final class Backend extends HttpServlet {

    private final long TIMEOUT = 8 * 60 * 1000;     // 8 minutes
    private final Logger log = LoggerFactory.getLogger(Backend.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final Thread servletThread = Thread.currentThread();
        final LinkedList<QueueItem> queue = new LinkedList<>();
        final EventPlexer.PlexerListener listener = new EventPlexer.PlexerListener() {
            @Override
            public void onNewLink(QueueItem item) {
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
        PrintWriter out = resp.getWriter();

        out.write(":\n\n");
        out.flush();

        while (!Thread.interrupted()) {
            try {
                QueueItem next;
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
                    out.write(":\n\n");
                    out.flush();
                    continue;
                }

                log.debug("{}: Sending link: {}", req.getRemoteAddr(), next);
                out.write(buildEvent(next));
                out.flush();

                if (out.checkError()) {
                    log.info("{}: Remote end closed connection", req.getRemoteAddr());
                    break;
                }
            } catch (InterruptedException e) {
                log.info("Closing down");
                out.write("close: closed\n\n");
                out.flush();
                out.close();
                break;
            }
        }

        EventPlexer.getInstance().removeListener(listener);
        log.debug("{}: No longer connected", req.getRemoteAddr());
    }

    private String buildEvent(QueueItem item) {
        return
                "event: " +
                item.action
                .toString()
                .toLowerCase() +
                "\ndata: " +
                item.link
                        .toJson()
                        .toString() +
                "\n\n";

    }
}