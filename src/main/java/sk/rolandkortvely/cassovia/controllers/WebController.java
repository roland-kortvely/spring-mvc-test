package sk.rolandkortvely.cassovia.controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import sk.rolandkortvely.cassovia.helpers.Hash;
import sk.rolandkortvely.cassovia.models.User;
import sk.rolandkortvely.cassovia.models.UserGroup;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main Web Controller
 */
@Controller
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
     * Redirects to /api/install
     */
    @RequestMapping("/install")
    public void install(HttpServletResponse response) {
        redirect(response, "/api/install");
    }

    /**
     * @param response Server Response to Client request
     * @param model    Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return login view (Thymeleaf, HTML file)
     */
    @RequestMapping("/login")
    public String login(HttpServletResponse response, Model model) {

        //Redirect authenticated users to homepage
        if (isLoggedIn()) {
            redirect(response);
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
     */
    @PostMapping(value = "/auth")
    public void auth(HttpServletResponse response, @ModelAttribute User user) {

        //Redirect authenticated users to homepage
        if (isLoggedIn()) {
            redirect(response);
            return;
        }

        if (!login(user)) {
            error("Wrong credentials!");
            redirect(response, "/login");
            return;
        }

        //redirect client to homepage
        redirect(response, "/");
    }

    /**
     * Logout user
     *
     * @param response Server Response to Client request
     */
    @RequestMapping("/logout")
    public void _logout(HttpServletResponse response) {
        logout();
        redirect(response, "/");
    }

    /**
     * Redirect client to /admin/users
     *
     * @param response Server Response to Client request
     */
    @RequestMapping("/admin")
    public void admin(HttpServletResponse response) {
        protectAdmin();
        redirect(response, "/admin/users");
    }

    /* User Resource */

    /**
     * List of Users
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Thymeleaf View, list of all users
     */
    @RequestMapping("/admin/users")
    public String users(Model model) {
        protectAdmin();

        model.addAttribute("user", new User()); //Because we want to be able to delete users
        model.addAttribute("users", new User().all()); //List of all users

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
        protectAdmin();

        model.addAttribute("user", new User());  //Because we want to be able to create a new user
        model.addAttribute("groups", new UserGroup().all()); //Because we want to be able to assign a role to newly created user

        return "admin/users/create";
    }

    /**
     * Edit User
     *
     * @param response Server Response to Client request
     * @param model    Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @param id       User ID we are going to edit
     * @return Thymeleaf View, form to edit an existing user
     */
    @RequestMapping("/admin/users/{id}")
    public String users_edit(HttpServletResponse response, Model model, @PathVariable Integer id) {
        protectAdmin();

        User user = new User().find(id);
        if (user == null) {
            error("User not found!");
            redirect(response, "/admin/users");
            return "admin/users/index";
        }

        model.addAttribute("user", user);
        model.addAttribute("groups", new UserGroup().all());

        return "admin/users/create";
    }

    /**
     * Delete user
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     */
    @PostMapping("/admin/users/delete")
    public void users_delete(HttpServletResponse response, @ModelAttribute User data) {
        protectAdmin();

        if (data.getId() == 0) {
            error("Unknown user");
            redirect(response, "/admin/users");
            return;
        }

        if (auth().getId() == data.getId()) {
            error("You cannot delete yourself");
            redirect(response, "/admin/users");
            return;
        }

        User user = new User().find(data.getId());
        if (user == null) {
            error("User not found!");
            redirect(response, "/admin/users");
            return;
        }

        user.delete();

        info("User deleted!");

        redirect(response, "/admin/users");
    }

    /**
     * Store user in database
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     */
    @PostMapping("/admin/users/store")
    public void users_store(HttpServletResponse response, @ModelAttribute User data) {
        users_store_raw(response, data, "/admin/users");
    }

    private void users_store_raw(HttpServletResponse response, @ModelAttribute User data, String uri) {
        protectAdmin();

        /*
         * Data validation
         * We want to validate posted username, whether it is not in use
         */
        if (new User().stream()
                .filter(u -> u.getUsername().equals(data.getUsername()))
                .filter(u -> u.getId() != data.getId()) //we want to be able to update current user (same username)
                .findFirst().orElse(null) != null
        ) {
            error("Username already in use!");
            redirect(response, uri);
            return;
        }

        User user;

        if (data.getId() != 0) {
            /*
             * Search for user we want to update
             */
            user = new User().find(data.getId());
            if (user == null) {
                error("User not found!");
                redirect(response, uri);
                return;
            }

            info("User updated!");
        } else {

            /*
             * Validate password is set
             */
            if (data.getPassword().length() < 1) {
                error("You need to set password!");
                redirect(response, uri);
                return;
            }

            /*
             * New empty user we want to store in database
             */
            user = new User();
            info("User created!");

            //Send welcome email
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("cassovia@example.com");
                message.setTo(data.getEmail());
                message.setSubject("Welcome, " + data.getUsername() + "!");
                message.setText("Your account has been successfully created");
                emailSender.send(message);
            } catch (Exception e) {
                System.out.println("Unable to send email.");
            }
        }

        //Copy form data to empty user we will store in database
        user.setUsername(data.getUsername());
        user.setEmail(data.getEmail());

        if (data.getPassword().length() > 1) {
            user.setPassword(Hash.make(data.getPassword())); //Hash password
        }

        user.setRole(data.getRole());
        user.setGroups(data.getGroups());

        user.save(); //Store in database

        redirect(response, uri);
    }

    /**
     * Generate PDF from User list
     *
     * @param response Server Response to Client request
     */
    @RequestMapping("/admin/users/export")
    public void users_export(HttpServletResponse response) {
        protectAdmin();

        //Create new empty PDF
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream("users.pdf")); //Save PDF to users.pdf
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unknown error");
        }

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

        try {
            document.add(table);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unknown error");
        }

        //Save each user to table row
        new User().all().forEach(user -> {

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
            group.setPhrase(new Phrase(user.isAdmin() ? "admin" : "user"));
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

    /* UserGroup Resource */

    /**
     * List of User Groups
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Thymeleaf View, list of all user groups
     */
    @RequestMapping("/admin/groups")
    public String groups(Model model) {

        protectAdmin();

        model.addAttribute("group", new UserGroup());
        model.addAttribute("groups", new UserGroup().all());

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
        protectAdmin();

        model.addAttribute("group", new UserGroup());
        model.addAttribute("users", new User().all());

        return "admin/groups/create";
    }


    /**
     * List of Users in Group
     *
     * @param model Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @return Thymeleaf View, list of all users
     */
    @RequestMapping("/admin/groups/{id}")
    public String groups_users(HttpServletResponse response, Model model, @PathVariable Integer id) {
        protectAdmin();

        UserGroup group = new UserGroup().find(id);
        if (group == null) {
            error("Group not found!");
            redirect(response, "/admin/groups");
            return "admin/groups/index";
        }

        model.addAttribute("user", new User());
        model.addAttribute("group", group);
        model.addAttribute("users", new User()
                .stream()
                .filter(user -> user.assignedInGroup(group))
                .collect(Collectors.toList())
        );

        return "admin/groups/users";
    }

    /**
     * Edit Group
     *
     * @param response Server Response to Client request
     * @param model    Instance of empty object for Thymeleaf, you fill model with data you want to share with View
     * @param id       User Group ID we are going to edit
     * @return Thymeleaf View, form to edit an existing user group
     */
    @RequestMapping("/admin/groups/{id}/edit")
    public String groups_edit(HttpServletResponse response, Model model, @PathVariable Integer id) {
        protectAdmin();

        UserGroup group = new UserGroup().find(id);
        if (group == null) {
            error("Group not found!");
            redirect(response, "/admin/groups");
            return "admin/groups/index";
        }

        model.addAttribute("group", group);
        model.addAttribute("users", new User().all());

        return "admin/groups/create";
    }

    /**
     * Create User and assign him to UserGroup
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     */
    @PostMapping("/admin/groups/{id}/store-user")
    public void groups_store_user(HttpServletResponse response, @PathVariable Integer id, @ModelAttribute User data) {
        protectAdmin();

        UserGroup group = new UserGroup().find(id);
        if (group == null) {
            error("Group not found!");
            redirect(response, "/admin/groups");
            return;
        }

        data.assignGroup(group);

        users_store_raw(response, data, "/admin/groups/" + id);
    }

    /**
     * Remove User from UserGroup
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     */
    @PostMapping("/admin/groups/{id}/delete-user")
    public void groups_delete_user(HttpServletResponse response, @PathVariable Integer id, @ModelAttribute User data) {
        protectAdmin();

        UserGroup group = new UserGroup().find(id);
        if (group == null) {
            error("Group not found!");
            redirect(response, "/admin/groups");
            return;
        }

        /*
         * Search for user we want to remove from group
         */
        User user = new User().find(data.getId());
        if (user == null) {
            error("User not found!");
            redirect(response, "/admin/groups/" + id);
            return;
        }

        user.discardGroup(group);
        user.save();

        info("User removed from group!");

        redirect(response, "/admin/groups/" + id);
    }

    /**
     * Delete User Group
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     */
    @PostMapping("/admin/groups/delete")
    public void groups_delete(HttpServletResponse response, @ModelAttribute UserGroup data) {
        protectAdmin();

        if (data.getId() == 0) {
            error("Unknown group");
            redirect(response, "/admin/groups");
            return;
        }

        UserGroup group = new UserGroup().find(data.getId());
        if (group == null) {
            error("Group not found!");
            redirect(response, "/admin/groups");
            return;
        }

        group.delete();

        info("Group deleted!");

        redirect(response, "/admin/groups");
    }

    /**
     * Store User Group in database
     *
     * @param response Server Response to Client request
     * @param data     POST data from form
     */
    @PostMapping("/admin/groups/store")
    public void groups_store(HttpServletResponse response, @ModelAttribute UserGroup data) {
        protectAdmin();

        UserGroup group;

        if (data.getId() != 0) {
            group = new UserGroup().find(data.getId());
            if (group == null) {
                error("Group not found!");
                redirect(response, "/admin/groups");
                return;
            }

            info("Group updated!");
        } else {
            group = new UserGroup();
            info("Group created!");
        }

        group.setGroupName(data.getGroupName());
        group.save();

        group.setUsers(data.getUsers());
        group.save();

        redirect(response, "/admin/groups");
    }
}
