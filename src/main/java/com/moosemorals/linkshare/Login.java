package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Login extends HttpServlet {

    private static final String[] REQUIRED_FIELDS = {"username", "password", "device"};

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        for (String field : REQUIRED_FIELDS) {
            String value = req.getParameter(field);
            if (value == null || value.isEmpty()) {
                Globals.sendError(resp, "Missing parameter '" + field + "'");
                return;
            }
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String device = req.getParameter("device");

        AuthManager auth = AuthManager.getInstance();

        User user = auth.checkAuth(username, password);
        if (user != null) {
            JsonObject json = Json.createObjectBuilder()
                    .add("user", user.getName())
                    .add("token", auth.createToken(user, device))
                    .build();
            Globals.sendSuccess(resp, json);
        } else {
            Globals.sendError(resp, HttpServletResponse.SC_FORBIDDEN,"Login Failed");
        }
    }
}
