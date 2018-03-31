package com.moosemorals.linkshare;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    static final String USER = "com.moosemorals.user";
    private static final String BASIC = "basic";
    private final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            throw new ServletException("Request/Response must be HTTP");
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
/*
        if (!req.isSecure()) {
            URI reqURI;
            try {
                reqURI = new URI(req.getRequestURI());
                URI redirectURI = new URI("https", reqURI.getSchemeSpecificPart(), reqURI.getFragment());
                resp.sendRedirect(redirectURI.toString());
            } catch (URISyntaxException e) {
                throw new ServletException("Broken URIs");
            }
        }
*/
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null) {
            User user = checkAuth(authHeader);
            if (user != null) {
                req.setAttribute(USER, user );
                chain.doFilter(req, resp);
                return;
            }
        }
        resp.setHeader("WWW-Authenticate", "Basic realm=\"link-share\"");
        resp.setStatus(401);
    }

    @Override
    public void destroy() {

    }

    private User checkAuth(String authHeader) {

        if (!authHeader.toLowerCase().startsWith(BASIC)) {
            return null;
        }

        String base64 = authHeader.substring(BASIC.length() + 1);

        String creds = new String(new Base64().decode(base64));

        if (!creds.contains(":")) {
            return null;
        }

        String[] parts = creds.split(":");
        if (parts.length != 2) {
            return null;
        }

        return AuthManager.getInstance().checkAuth(parts[0], parts[1]);
    }
}
