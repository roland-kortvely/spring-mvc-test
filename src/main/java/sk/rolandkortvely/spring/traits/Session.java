package sk.rolandkortvely.spring.traits;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpSession;

/**
 * Session Trait
 */
public interface Session
{

    /**
     * @param session session context (in browser)
     * @param msg     String message to save
     */
    default void info(@NotNull HttpSession session, @NotNull String msg)
    {
        session.setAttribute("info", msg);
    }

    /**
     * @param session session context (in browser)
     */
    default String getInfo(@NotNull HttpSession session)
    {
        String msg = (String) session.getAttribute("info");
        session.removeAttribute("info");
        return msg;
    }

    /**
     * @param session session context (in browser)
     * @param msg     String message to save
     */
    default void error(@NotNull HttpSession session, @NotNull String msg)
    {
        session.setAttribute("errors", msg);
    }

    /**
     * @param session session context (in browser)
     */
    default String getError(@NotNull HttpSession session)
    {
        String error = (String) session.getAttribute("errors");
        session.removeAttribute("errors");
        return error;
    }
}
