package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet({"/links", "/links/*"})
public final class LinksServlet extends HttpServlet {

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String id = req.getPathInfo();

        if (id == null) {
            Globals.sendError(resp, "Missing id");
            return;
        }
        id = id.substring(1);   // remove leading slash

        if (!Globals.validateId(id)) {
            Globals.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Invalid id");
            return;
        }

        User user = (User) req.getAttribute(AuthFilter.USER);

        LinkManager links = LinkManager.getInstance();
        Link link = links.deleteLink(user, id);
        if (link != null) {
            EventPlexer.getInstance().queueLink(EventPlexer.Action.DELETED, link);
            Globals.sendSuccess(resp, id);
        } else {
            Globals.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Not your link");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        long latest = 0;

        String strLatest = req.getParameter("latest");
        if (strLatest != null && !strLatest.isEmpty()) {
            try {
                latest = Long.parseLong(strLatest, 0);
            } catch (NumberFormatException ex) {
                // ignored
            }
        }

        List<Link> links = LinkManager.getInstance().getLinks(latest);

        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Link l : links) {
            json.add(l.toJson());
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        try (JsonWriter out = Json.createWriter(resp.getWriter())) {
            out.write(json.build());
        }
    }
}
