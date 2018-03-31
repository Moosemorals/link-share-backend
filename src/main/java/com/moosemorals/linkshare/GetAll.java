package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/links")
public final class GetAll extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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
        for (Link l : links ) {
            json.add(l.toJson());
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        try (JsonWriter out = Json.createWriter(resp.getWriter())) {
            out.write(json.build());
        }
    }
}
