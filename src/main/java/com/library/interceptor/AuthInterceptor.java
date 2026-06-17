package com.library.interceptor;

import com.library.model.Role;
import com.library.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI().substring(request.getContextPath().length());

        HttpSession session = request.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("currentUser");

        if (user == null) {
            LOGGER.warn("Unauthenticated access attempt to {}", path);
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (path.startsWith("/admin") && user.getRole() != Role.ADMIN) {
            return denyAccess(request, response, path, user);
        }

        if (path.startsWith("/librarian") && user.getRole() != Role.LIBRARIAN) {
            return denyAccess(request, response, path, user);
        }

        if (path.startsWith("/requests") && user.getRole() != Role.READER) {
            return denyAccess(request, response, path, user);
        }

        if (path.startsWith("/account") && user.getRole() != Role.READER) {
            return denyAccess(request, response, path, user);
        }

        return true;
    }

    private boolean denyAccess(HttpServletRequest request,
                               HttpServletResponse response,
                               String path,
                               User user) throws Exception {
        LOGGER.warn("Access denied for user {} (role {}) to {}",
                user.getUsername(), user.getRole(), path);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/WEB-INF/views/error-403.html").forward(request, response);
        return false;
    }
}