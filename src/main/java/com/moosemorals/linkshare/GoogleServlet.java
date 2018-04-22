package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class GoogleServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(GoogleServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User)req.getAttribute(AuthFilter.USER);

        String token = req.getParameter("token");

        if (token != null && !token.isEmpty()) {
            user.addPhone(token);
            Globals.sendSuccess(resp, "Phone registered");
            log.debug("Added new phone for {}", user.getName());
        } else {
            Globals.sendError(resp, "Missing phone token");
            log.warn("Can't add phone for {}: Missing token", user.getName());
        }
    }
}
