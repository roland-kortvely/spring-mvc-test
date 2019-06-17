package sk.rolandkortvely.cassovia.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import sk.rolandkortvely.cassovia.DB;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
public class UserGroup extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;
    private String groupName;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_group",
            joinColumns = {@JoinColumn(name = "group_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<User> users = new HashSet<>();

    public static UserGroup find(Integer id) {
        return query()
                .where("id", id)
                .stream()
                .findFirst().orElse(null);
    }

    public static List<UserGroup> all() {
        return query()
                .stream()
                .collect(Collectors.toList());
    }

    public static Stream<UserGroup> stream() {
        return stream(UserGroup.class);
    }

    public static QueryStream<UserGroup> query() {
        return new QueryStream<>(UserGroup.class, DB.sessionFactory, "groupName");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
