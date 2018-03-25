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
    private final Logger log = LoggerFactory.getLogger(Backend.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final LinkedList<Link> queue = new LinkedList<>();
        final EventPlexer.PlexerListener listener = new EventPlexer.PlexerListener() {
            @Override
            public void onNewLink(Link link) {
                synchronized (queue) {
                    queue.addLast(link);
                    queue.notifyAll();
                }
            }
        };

        log.info("{}: Connected", req.getRemoteAddr());

        EventPlexer.getInstance().addListener(listener);

        resp.setHeader("Cache-Control", "no-cache");
        resp.setContentType("text/event-stream;charset=utf-8");

        PrintWriter out = resp.getWriter();

        out.write(":\n\n");
        out.flush();

        while (!Thread.interrupted()) {
            try {
                Link next;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        queue.wait();
                    }

                    next = queue.removeFirst();
                }

                log.debug("{}: Sending link: {}", req.getRemoteAddr(), next);
                out.write(buildEvent(next));
                out.flush();

                if (out.checkError()) {
                    log.info("{}: Remote end closed connection", req.getRemoteAddr());
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        EventPlexer.getInstance().removeListener(listener);
        log.debug("{}: No longer connected", req.getRemoteAddr());
    }

    private String buildEvent(Link link) {
        StringBuilder message = new StringBuilder();

        message.append("id: ").append(link.getId()).append("\n");
        message.append("data: ").append(link.getLink()).append("\n");
        message.append("\n\n");

        return message.toString();
    }
}