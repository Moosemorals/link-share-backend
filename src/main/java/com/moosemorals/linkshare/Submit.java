package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class Submit extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(Submit.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getPathInfo() != null) {
            Globals.sendError(resp, "Can't post with a path");
            return;
        }

        String link = req.getParameter("url");

        if (link == null || link.isEmpty()) {
            Globals.sendError(resp, "Must include link");
            return;
        }

        // Other fields are optional
        String title = req.getParameter("title");
        String favIconURL = req.getParameter("favIconURL");

        User user = (User) req.getAttribute(AuthFilter.USER);

        log.info("{}: New link: {}", req.getRemoteAddr(), link);
        Link result = LinkManager.getInstance().createLink(user, link, title, favIconURL);

        Globals.sendSuccess(resp, result.toJson());
    }
}
