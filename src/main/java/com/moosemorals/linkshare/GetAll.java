package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
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
                latest = 0;
            }
        }

        List<Link> links = LinkManager.getInstance().getLinks(latest);

        JsonArrayBuilder json = Json.createArrayBuilder();
        for (Link l : links ) {
            json.add(l.toJson());
        }

        String result = json.build().toString();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setContentLength(result.length());
        resp.getWriter().write(result);

    }
}
