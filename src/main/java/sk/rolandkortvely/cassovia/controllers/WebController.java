package sk.rolandkortvely.cassovia.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

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

        if (User.stream(sessionFactory)
                .filter(u -> u.getUsername().equals(data.getUsername()))
                .filter(u -> u.getId() != data.getId())
                .findFirst().orElse(null) != null
        ) {
            error("Username already in use!");
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
            flash("info", "User created!");
        }

        user.setUsername(data.getUsername());
        user.setEmail(data.getEmail());
        user.setPassword(Hash.make(data.getPassword()));
        user.setRole(group);

        user.save();

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    @RequestMapping("/admin/users/export")
    public void users_export(HttpServletResponse response) throws Exception {
        this.protectAdmin();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("users.pdf"));

        document.open();

        PdfPTable table = new PdfPTable(4);

        Stream.of("#", "username", "email", "role")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
        document.add(table);

        List<User> list = User.all(sessionFactory);

        list.forEach(user -> {

            PdfPTable t = new PdfPTable(4);

            PdfPCell id = new PdfPCell();
            id.setPhrase(new Phrase(user.getId() + ""));
            t.addCell(id);

            PdfPCell username = new PdfPCell();
            username.setPhrase(new Phrase(user.getUsername()));
            t.addCell(username);

            PdfPCell email = new PdfPCell();
            email.setPhrase(new Phrase(user.getEmail()));
            t.addCell(email);

            PdfPCell group = new PdfPCell();
            group.setPhrase(new Phrase(user.getRole().getGroupName()));
            t.addCell(group);

            t.completeRow();

            try {
                document.add(t);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        });

        document.close();

        try {
            InputStream is = new FileInputStream("users.pdf");
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    @RequestMapping("/admin/groups")
    public String groups(Model model) {

        this.protectAdmin();

        model.addAttribute("group", new UserGroup());
        model.addAttribute("groups", UserGroup.all(sessionFactory));

        return "admin/groups/index";
    }

    @RequestMapping("/admin/groups/create")
    public String groups_create(Model model) {
        this.protectAdmin();

        model.addAttribute("user", new User());
        model.addAttribute("group", new UserGroup());
        model.addAttribute("groups", UserGroup.all(sessionFactory));

        return "admin/groups/create";
    }

    @RequestMapping("/admin/groups/{id}")
    public String groups_edit(HttpServletResponse response, Model model, @PathVariable Integer id) throws Exception {
        this.protectAdmin();

        UserGroup group = UserGroup.find(sessionFactory, id);
        if (group == null) {
            error("Group not found!");
            response.sendRedirect(request.getContextPath() + "/admin/groups");
            return "admin/groups/index";
        }

        model.addAttribute("user", new User());
        model.addAttribute("group", group);
        model.addAttribute("groups", UserGroup.all(sessionFactory));

        return "admin/groups/create";
    }

    @PostMapping("/admin/groups/delete")
    public void groups_delete(HttpServletResponse response, @ModelAttribute UserGroup data) throws Exception {
        this.protectAdmin();

        if (data.getId() == 0) {
            error("Unknown group");
            response.sendRedirect(request.getContextPath() + "/admin/groups");
            return;
        }

        UserGroup group = UserGroup.find(sessionFactory, data.getId());
        if (group == null) {
            error("Group not found!");
            response.sendRedirect(request.getContextPath() + "/admin/groups");
            return;
        }

        if (group.getGroupName().equals("admin")) {
            error("You cannot delete admin group!");
            response.sendRedirect(request.getContextPath() + "/admin/groups");
            return;
        }

        group.delete();

        flash("info", "Group deleted!");

        response.sendRedirect(request.getContextPath() + "/admin/groups");
    }

    @PostMapping("/admin/groups/store")
    public void groups_store(HttpServletResponse response, @ModelAttribute UserGroup data) throws Exception {
        this.protectAdmin();

        UserGroup group;

        if (data.getId() != 0) {
            group = UserGroup.find(sessionFactory, data.getId());
            if (group == null) {
                error("Group not found!");
                response.sendRedirect(request.getContextPath() + "/admin/groups");
                return;
            }

            flash("info", "Group updated!");
        } else {
            group = new UserGroup(sessionFactory);
            flash("info", "Group created!");
        }

        group.setGroupName(data.getGroupName());

        group.save();

        response.sendRedirect(request.getContextPath() + "/admin/groups");
    }
}
