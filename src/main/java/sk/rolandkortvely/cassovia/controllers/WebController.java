package sk.rolandkortvely.cassovia.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;
import sk.rolandkortvely.cassovia.helpers.Hash;
import sk.rolandkortvely.cassovia.models.User;
import sk.rolandkortvely.cassovia.models.UserGroup;

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
    public String login(HttpServletResponse response, Model model) {
        if (guestRedirect(response)) {
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
    public void admin(HttpServletResponse response) throws Exception {

        this.protect();

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    @PostMapping(value = "/auth")
    public void auth(HttpServletResponse response, @ModelAttribute User user) throws Exception {

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

        model.addAttribute("user", new User());
        model.addAttribute("users", User.all(sessionFactory));

        return "admin/users/index";
    }

    @RequestMapping("/admin/users/create")
    public String users_create(Model model) {
        this.protectAdmin();

        model.addAttribute("user", new User());
        model.addAttribute("groups", UserGroup.all(sessionFactory));

        return "admin/users/create";
    }

    @RequestMapping("/admin/users/{id}")
    public String users_edit(HttpServletResponse response, Model model, @PathVariable Integer id) throws Exception {
        this.protectAdmin();

        User user = User.find(sessionFactory, id);
        if (user == null) {
            error("User not found!");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return "admin/users/index";
        }

        model.addAttribute("user", user);
        model.addAttribute("groups", UserGroup.all(sessionFactory));

        return "admin/users/create";
    }

    @PostMapping("/admin/users/delete")
    public void users_delete(HttpServletResponse response, @ModelAttribute User data) throws Exception {
        this.protectAdmin();

        if (data.getId() == 0) {
            error("Unknown user");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        if (auth().getId() == data.getId()) {
            error("You cannot delete yourself");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        System.out.println("deleting " + data.getId());

        User user = User.find(sessionFactory, data.getId());
        if (user == null) {
            error("User not found!");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        user.delete();

        flash("info", "User deleted!");

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    @PostMapping("/admin/users/store")
    public void users_store(HttpServletResponse response, @ModelAttribute User data) throws Exception {
        this.protectAdmin();

        UserGroup group = UserGroup.find(sessionFactory, data.getRole().getId());
        if (group == null) {
            error("Unknown role selected");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        User user;

        if (data.getId() != 0) {
            user = User.find(sessionFactory, data.getId());
            if (user == null) {
                error("User not found!");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }

            flash("info", "User updated!");
        } else {
            user = new User(sessionFactory);
            //data.save();
            flash("info", "User created!");
        }

        user.setUsername(data.getUsername());
        user.setEmail(data.getEmail());
        user.setPassword(Hash.make(data.getPassword()));
        user.setRole(group);

        user.save();

        response.sendRedirect(request.getContextPath() + "/admin/users");
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
}
