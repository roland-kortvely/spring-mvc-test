package sk.rolandkortvely.cassovia.models;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User extends Model<User> {

    public User() {
        super(User.class);
    }

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
        groups.stream()
                .filter(group -> group.getId() == userGroup.getId())
                .findFirst()
                .ifPresent(groups::remove);
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
