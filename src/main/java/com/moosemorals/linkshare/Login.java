package com.moosemorals.linkshare;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Login extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isEmpty()) {
            Globals.sendError(resp, "Missing username");
            return;
        }

        if (password == null || password.isEmpty()) {
            Globals.sendError(resp, "Missing password");
        }

        AuthManager auth = AuthManager.getInstance();

        User user = auth.checkAuth(username, password);
        if (user != null) {
            JsonObject json = Json.createObjectBuilder()
                    .add("user", user.toJson())
                    .add("token", auth.createToken(user))
                    .build();
            Globals.sendSuccess(resp, json);
        } else {
            Globals.sendError(resp, "Login Failed");
        }
    }
}
