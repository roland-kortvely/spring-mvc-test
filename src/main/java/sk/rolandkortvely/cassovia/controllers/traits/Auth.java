package sk.rolandkortvely.cassovia.controllers.traits;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sk.rolandkortvely.cassovia.entities.User;
import sk.rolandkortvely.cassovia.helpers.Crypto;
import sk.rolandkortvely.cassovia.helpers.Hash;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public interface Auth {

    default boolean login(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, @NotNull HttpServletRequest request, User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        if (user.getUsername().length() == 0 || user.getPassword().length() == 0) {
            return false;
        }

        List<User> users = User.all(sessionFactory);
        User q = users.stream()
                .filter(u -> user.getUsername().equals(u.getUsername()))
                .filter(u -> Hash.make(user.getPassword()).equals(u.getPassword()))
                .findFirst().orElse(null);

        if (q == null) {
            return false;
        }

        session.setAttribute("auth", Crypto.encrypt(q.getPassword(), getClientIp(request)));
        return true;
    }

    default void logout(@NotNull HttpSession session, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        session.removeAttribute("auth");
        try {
            response.sendRedirect(request.getContextPath() + "/");
        } catch (IOException e) {
            return;
        }
    }

    default User auth(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, @NotNull HttpServletRequest request) {

        String auth = (String) session.getAttribute("auth");
        if (auth == null) {
            return null;
        }
        if (auth.length() == 0) {
            return null;
        }

        String hash = Crypto.decrypt(auth, getClientIp(request));
        if (hash == null) {
            return null;
        }

        return User.all(sessionFactory).stream()
                .filter(u -> hash.equals(u.getPassword()))
                .findFirst().orElse(null);
    }

    default boolean isLoggedIn(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, @NotNull HttpServletRequest request) {
        return auth(sessionFactory, session, request) != null;
    }

    default void protect(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, @NotNull HttpServletRequest request) {
        if (!isLoggedIn(sessionFactory, session, request)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please");
        }
    }

    default boolean guestRedirect(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        if (isLoggedIn(sessionFactory, session, request)) {

            try {
                response.sendRedirect(request.getContextPath() + "/");
            } catch (IOException e) {
                return true;
            }
            return true;
        }
        return false;
    }

    default String getClientIp(@NotNull HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || "".equals(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }
}
