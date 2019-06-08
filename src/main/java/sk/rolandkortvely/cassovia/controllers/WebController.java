package sk.rolandkortvely.cassovia.controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import sk.rolandkortvely.cassovia.helpers.Hash;
import sk.rolandkortvely.cassovia.models.User;
import sk.rolandkortvely.cassovia.models.UserGroup;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/")
public class WebController extends AbstractController {

    /**
     * @return Homepage view (Thymeleaf, HTML file)
     */
    @RequestMapping
    public String index() {
        return "index";
    }

    /**
     * @param response Server Response to Client request
     * @param model    Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return login view (Thymeleaf, HTML file)
     */
    @RequestMapping("/login")
    public String login(HttpServletResponse response, Model model) {

        //Redirect authenticated users to homepage
        if (authenticatedRedirect(response)) {
            return "error";
        }

        //Fill model with empty User
        model.addAttribute("user", new User());

        //Thymeleaf View with model
        return "login";
    }

    /**
     * Authorize user
     *
     * @param response Server Response to Client request
     * @param user     POST data from form
     * @throws Exception ..
     */
    @PostMapping(value = "/auth")
    public void auth(HttpServletResponse response, @ModelAttribute User user) throws Exception {

        //Redirect authenticated users to homepage
        if (authenticatedRedirect(response)) {
            return;
        }

        if (!this.login(user)) {
            error("Wrong credentials!");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        //redirect client to homepage
        response.sendRedirect(request.getContextPath() + "/");
    }

    /**
     * Logout user
     *
     * @param response Server Response to Client request
     */
    @RequestMapping("/logout")
    public String _logout(HttpServletResponse response) {
        logout(response);
        return "index";
    }

    /**
     * Redirect client to /admin/users
     *
     * @param response Server Response to Client request
     */
    @RequestMapping("/admin")
    public void admin(HttpServletResponse response) throws Exception {
        this.protectAdmin();
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    /**
     * List of Users
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Thymeleaf View, list of all users
     */
    @RequestMapping("/admin/users")
    public String users(Model model) {
        this.protectAdmin();

        model.addAttribute("user", new User()); //Because we want to be able to delete users
        model.addAttribute("users", User.all(sessionFactory)); //List of all users

        return "admin/users/index";
    }

    /**
     * Create User
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Thymeleaf View, form to create a new user
     */
    @RequestMapping("/admin/users/create")
    public String users_create(Model model) {
        this.protectAdmin();

        model.addAttribute("user", new User());  //Because we want to be able to create a new user
        model.addAttribute("groups", UserGroup.all(sessionFactory)); //Because we want to be able to assign a role to newly created user

        return "admin/users/create";
    }

    /**
     * Edit User
     *
     * @param response Server Response to Client request
     * @param model    Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @param id       User ID we are going to edit
     * @return Thymeleaf View, form to edit an existing user
     * @throws Exception ..
     */
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

    /**
     * Delete user
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     * @throws Exception ..
     */
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

    /**
     * Store user in database
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     * @throws Exception ..
     */
    @PostMapping("/admin/users/store")
    public void users_store(HttpServletResponse response, @ModelAttribute User data) throws Exception {
        this.protectAdmin();

        /*
         * Data validation
         * We want to validate posted group, whether it exists
         */
        UserGroup group = UserGroup.find(sessionFactory, data.getRole().getId());
        if (group == null) {
            error("Unknown role selected");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        /*
         * Data validation
         * We want to validate posted username, whether it is not in use
         */
        if (User.stream(sessionFactory)
                .filter(u -> u.getUsername().equals(data.getUsername()))
                .filter(u -> u.getId() != data.getId()) //we want to be able to update current user (same username)
                .findFirst().orElse(null) != null
        ) {
            error("Username already in use!");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        User user;

        if (data.getId() != 0) {
            /*
             * Search for user we want to update
             */
            user = User.find(sessionFactory, data.getId());
            if (user == null) {
                error("User not found!");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }

            flash("info", "User updated!");
        } else {
            /*
             * New empty user we want to store in database
             */
            user = new User(sessionFactory);
            flash("info", "User created!");

            //Send welcome email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("cassovia@example.com");
            message.setTo(data.getEmail());
            message.setSubject("Welcome, " + data.getUsername() + "!");
            message.setText("Your account has been successfully created");
            emailSender.send(message);
        }

        //Copy form data to empty user we will store in database
        user.setUsername(data.getUsername());
        user.setEmail(data.getEmail());
        user.setPassword(Hash.make(data.getPassword())); //Hash password
        user.setRole(group);

        user.save(); //Store in database

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    /**
     * Generate PDF from User list
     *
     * @param response Server Response to Client request
     * @throws Exception ..
     */
    @RequestMapping("/admin/users/export")
    public void users_export(HttpServletResponse response) throws Exception {
        this.protectAdmin();

        //Create new empty PDF
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("users.pdf")); //Save PDF to users.pdf

        document.open();

        //Table (id, username, email, group)
        PdfPTable table = new PdfPTable(4);

        //Table header
        Stream.of("#", "username", "email", "role")
                .forEach(title -> {
                    PdfPCell cell = new PdfPCell();
                    cell.setPhrase(new Phrase(title));
                    table.addCell(cell);
                });
        document.add(table);

        //Save each user to table row
        User.all(sessionFactory).forEach(user -> {

            PdfPTable row = new PdfPTable(4);

            PdfPCell id = new PdfPCell();
            id.setPhrase(new Phrase(user.getId() + ""));
            row.addCell(id);

            PdfPCell username = new PdfPCell();
            username.setPhrase(new Phrase(user.getUsername()));
            row.addCell(username);

            PdfPCell email = new PdfPCell();
            email.setPhrase(new Phrase(user.getEmail()));
            row.addCell(email);

            PdfPCell group = new PdfPCell();
            group.setPhrase(new Phrase(user.getRole().getGroupName()));
            row.addCell(group);

            try {
                document.add(row);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        });

        document.close();

        //Download PDF (Transfer it to the user)
        try {
            //Open PDF, once again.. why not..
            InputStream is = new FileInputStream("users.pdf");
            IOUtils.copy(is, response.getOutputStream()); //Copy PDF to buffer (Transfer it to the user)
            response.flushBuffer(); //Cleanup..
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    //ONCE AGAIN, ABSOLUTELY SAME, BUT TOTALLY DIFFERENT

    /**
     * List of User Groups
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Thymeleaf View, list of all user groups
     */
    @RequestMapping("/admin/groups")
    public String groups(Model model) {

        this.protectAdmin();

        model.addAttribute("group", new UserGroup());
        model.addAttribute("groups", UserGroup.all(sessionFactory));

        return "admin/groups/index";
    }

    /**
     * Create User Group
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Thymeleaf View, form to create a new user group
     */
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
