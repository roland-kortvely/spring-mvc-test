package sk.rolandkortvely.cassovia.controllers.traits;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpSession;

public interface Session {

    default void flash(@NotNull HttpSession session, @NotNull String key, @NotNull String msg) {
        session.setAttribute(key, msg);
    }

    default String getFlash(@NotNull HttpSession session, @NotNull String key) {
        String msg = (String) session.getAttribute(key);
        session.removeAttribute(key);
        return msg;
    }

    default void error(@NotNull HttpSession session, @NotNull String msg) {
        session.setAttribute("errors", msg);
    }

    default String getError(@NotNull HttpSession session) {
        String error = (String) session.getAttribute("errors");
        session.removeAttribute("errors");
        return error;
    }
}
