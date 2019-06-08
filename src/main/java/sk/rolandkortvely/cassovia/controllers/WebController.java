package sk.rolandkortvely.cassovia.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;
import sk.rolandkortvely.cassovia.entities.User;
import sk.rolandkortvely.cassovia.entities.UserGroup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/")
public class WebController extends AbstractController implements HandlerInterceptor {

    @RequestMapping
    public String index(Model model) {


        return "index";
    }

    @RequestMapping("/login")
    public String login(HttpServletResponse resp, Model model) {
        if (guestRedirect(resp)) {
            return "error";
        }

        model.addAttribute("user", new User());
        return "login";
    }

    @RequestMapping("/logout")
    public String log(HttpServletResponse response) {
        logout(response);
        return "index";
    }

    @RequestMapping("/admin")
    public void admin(HttpServletRequest request, HttpServletResponse response) throws Exception {

        this.protect();

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    @PostMapping(value = "/auth")
    public void auth(HttpServletRequest request, HttpServletResponse response, @ModelAttribute User user) throws Exception {

        if (this.isLoggedIn()) {
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }

        if (user.getUsername().length() == 0 || user.getPassword().length() == 0) {
            error("Wrong credentials!");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!login(user)) {
            error("Wrong credentials!");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/");
    }

    @RequestMapping("/admin/users")
    public String users(Model model) {

        this.protectAdmin();

        model.addAttribute("users", User.all(sessionFactory));

        return "admin/users";
    }

    @RequestMapping("/admin/groups")
    public String groups(Model model) {

        this.protectAdmin();

        model.addAttribute("groups", UserGroup.all(sessionFactory));

        return "admin/groups";
    }

    @RequestMapping("/admin/groups/add")
    public String group_add(Model model) {

        this.protectAdmin();

        model.addAttribute("group", new UserGroup());

        return "admin/groups-add";
    }

    @RequestMapping("/admin/users/add")
    public String user_add(Model model) {

        this.protectAdmin();

        model.addAttribute("user", new User());

        return "admin/groups-add";
    }
}
