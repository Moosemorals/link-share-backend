package com.moosemorals.linkshare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/submit")
public final class Submit extends HttpServlet{

	private final Logger log = LoggerFactory.getLogger(Submit.class);

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	    String link = req.getParameter("l");

	    if (link == null || link.isEmpty()) {
	        resp.sendError(400, "Bad or missing link parameter");
	        return;
        }

        log.info("{}: New link: {}", req.getRemoteAddr(), link);
        LinkManager.getInstance().createLink(link);

	    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
