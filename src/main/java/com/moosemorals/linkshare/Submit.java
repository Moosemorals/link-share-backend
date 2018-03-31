package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/submit")
public final class Submit extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(Submit.class);

    private static void sendError(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (JsonWriter jOut = Json.createWriter(resp.getWriter())) {
            jOut.write(Json.createObjectBuilder()
                    .add("error", message)
                    .build());
        }
    }

    private static void sendSuccess(HttpServletResponse resp, JsonValue success) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        try (JsonWriter jOut = Json.createWriter(resp.getWriter())) {
            jOut.write(Json.createObjectBuilder()
                    .add("success", success)
                    .build());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        if (req.getPathInfo() != null) {
            sendError(resp, "Can't post with a path");
            return;
        }

        String link = req.getParameter("l");

        if (link == null || link.isEmpty()) {
            sendError(resp, "Must include link");
            return;
        }

        // Other fields are optional
        String title = req.getParameter("t");
        String favIconURL = req.getParameter("f");
        String description = req.getParameter("d");

        User user = (User) req.getAttribute(AuthFilter.USER);

        log.info("{}: New link: {}", req.getRemoteAddr(), link);
        Link result = LinkManager.getInstance().createLink(user, link, title, favIconURL, description);

        sendSuccess(resp, result.toJson());
    }
}
