package com.library.interceptor;

import com.library.model.Role;
import com.library.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Cross-cutting access control. Runs before every controller method and enforces:
 * <ul>
 *   <li>URLs under /admin require the ADMIN role;</li>
 *   <li>URLs under /librarian require the LIBRARIAN role;</li>
 *   <li>URLs under /reader and /requests require any logged-in user.</li>
 * </ul>
 * Unauthenticated users are redirected to the login page; authenticated users without
 * the required role receive a 403 (access denied) page.
 */
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        HttpSession session = request.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("currentUser");

        // Not logged in → send to login.
        if (user == null) {
            LOGGER.warn("Unauthenticated access attempt to {}", path);
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // Role checks by URL prefix.
        if (path.startsWith("/admin") && user.getRole() != Role.ADMIN) {
            return denyAccess(request, response, path, user);
        }
        if (path.startsWith("/librarian") && user.getRole() != Role.LIBRARIAN
                && user.getRole() != Role.ADMIN) {
            return denyAccess(request, response, path, user);
        }

        return true;
    }

    /**
     * Sends a 403 response and logs the denied attempt.
     *
     * @param request  the current request
     * @param response the current response
     * @param path     the requested path
     * @param user     the logged-in user
     * @return always false to stop request processing
     * @throws Exception if forwarding fails
     */
    private boolean denyAccess(HttpServletRequest request, HttpServletResponse response,
                               String path, User user) throws Exception {
        LOGGER.warn("Access denied for user {} (role {}) to {}",
                user.getUsername(), user.getRole(), path);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/WEB-INF/views/error-403.html").forward(request, response);
        return false;
    }
}