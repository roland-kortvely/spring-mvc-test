package sk.samuelkarabas.cassovia.entities;

import org.hibernate.SessionFactory;

import javax.persistence.*;
import java.util.List;

@Entity
public class User extends AbstractModel<User> {

    public User() {
    }

    public User(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    private String username;
    private String password;

    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role", nullable = false)
    private UserGroup role;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserGroup getRole() {
        return role;
    }

    public void setRole(UserGroup role) {
        this.role = role;
    }

    public static User find(SessionFactory sessionFactory, Integer id) {
        return find(User.class, sessionFactory, id);
    }

    public static List<User> all(SessionFactory sessionFactory) {
        return all(User.class, sessionFactory);
    }
}
