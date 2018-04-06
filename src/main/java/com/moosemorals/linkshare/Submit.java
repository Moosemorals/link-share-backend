package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class Submit extends HttpServlet {

    private static final String[] REQUIRED_FIELDS = {"url", "to"};

    private final Logger log = LoggerFactory.getLogger(Submit.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AuthManager authManager = AuthManager.getInstance();
        LinkManager linkManager = LinkManager.getInstance();

        for (String field : REQUIRED_FIELDS) {
            String value = req.getParameter(field);
            if (value == null || value.isEmpty()) {
                Globals.sendError(resp, "Missing parameter: " + field);
                return;
            }
        }

        // Required fields
        String link = req.getParameter("url");
        User to = authManager.getUserByName(req.getParameter("to"));

        if (to == null) {
            Globals.sendError(resp, "User '" + req.getParameter("to") + "' not known");
            return;
        }

        // Other fields are optional
        String title = req.getParameter("title");
        String favIconURL = req.getParameter("favIconURL");

        User from = (User) req.getAttribute(AuthFilter.USER);

        log.info("{}: New link: {}", req.getRemoteAddr(), link);
        Link result = linkManager.createLink(from, to, link, title, favIconURL);

        Globals.sendSuccess(resp, result.toJson());
    }
}
