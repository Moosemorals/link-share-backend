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

    private final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private static final String BASIC = "basic";

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
        if (authHeader != null && checkAuth(authHeader)) {
            chain.doFilter(req, resp);
        } else {
            resp.setHeader("WWW-Authenticate", "Basic realm=\"link-share\"");
            resp.setStatus(401);
        }

    }

    @Override
    public void destroy() {

    }

    private boolean checkAuth(String authHeader) {

        if (!authHeader.toLowerCase().startsWith(BASIC)) {
            return false;
        }

        String base64 = authHeader.substring(BASIC.length() + 1);

        String creds = new String(new Base64().decode(base64));

        if (!creds.contains(":")) {
            return false;
        }

        String[] parts = creds.split(":");
        if (parts.length != 2) {
            return false;
        }

        return AuthManager.getInstance().checkAuth(parts[0], parts[1]);
    }
}
