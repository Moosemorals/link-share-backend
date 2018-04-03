package com.moosemorals.linkshare;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

final public class Logout extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getAttribute(AuthFilter.USER);
        String token = (String) req.getAttribute(AuthFilter.TOKEN);

        AuthManager.getInstance().logout(user, token);

        Globals.sendSuccess(resp, "Logged out");
    }
}
