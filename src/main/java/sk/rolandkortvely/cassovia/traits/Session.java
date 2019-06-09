package sk.rolandkortvely.cassovia.traits;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpSession;

/**
 * Session Trait
 */
public interface Session {

    /**
     * @param session session context (in browser)
     * @param key     String session key, ID
     * @param msg     String message to save
     */
    default void flash(@NotNull HttpSession session, @NotNull String key, @NotNull String msg) {
        session.setAttribute(key, msg);
    }

    /**
     * @param session session context (in browser)
     * @param key     String session key, ID
     */
    default String getFlash(@NotNull HttpSession session, @NotNull String key) {
        String msg = (String) session.getAttribute(key);
        session.removeAttribute(key);
        return msg;
    }

    /**
     * @param session session context (in browser)
     * @param msg     String message to save
     */
    default void error(@NotNull HttpSession session, @NotNull String msg) {
        session.setAttribute("errors", msg);
    }

    /**
     * @param session session context (in browser)
     */
    default String getError(@NotNull HttpSession session) {
        String error = (String) session.getAttribute("errors");
        session.removeAttribute("errors");
        return error;
    }
}
