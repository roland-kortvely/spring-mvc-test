package sk.rolandkortvely.cassovia.models;

import sk.rolandkortvely.cassovia.DB;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class User extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    private String username;
    private String password;
    private String email;
    private String token;

    private Boolean role = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_group",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "group_id")}
    )
    private Set<UserGroup> groups = new HashSet<>();

    public static User find(Integer id) {
        return query()
                .where("id", id)
                .stream()
                .findFirst().orElse(null);
    }

    public static List<User> all() {
        return query()
                .stream()
                .collect(Collectors.toList());
    }

    public static QueryStream<User> query() {
        return new QueryStream<>(User.class, DB.sessionFactory);
    }

    public static Stream<User> stream() {
        return stream(User.class);
    }

    public int getId() {
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<UserGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<UserGroup> groups) {
        this.groups = groups;
    }

    public boolean assignedInGroup(UserGroup userGroup) {
        return groups
                .stream()
                .filter(group -> group.getGroupName().equals(userGroup.getGroupName()))
                .findFirst()
                .orElse(null) != null;
    }

    public void assignGroup(UserGroup userGroup) {
        this.groups.add(userGroup);
    }

    public void discardGroup(UserGroup userGroup) {
        for (UserGroup group : groups) {
            if (group.getId() == userGroup.getId()) {
                this.groups.remove(group);
            }
        }
    }

    public Boolean getRole() {
        return role;
    }

    public void setRole(Boolean role) {
        this.role = role;
    }

    public Boolean isAdmin() {
        return this.getRole();
    }

    public void setAdmin() {
        this.setRole(true);
    }
}
