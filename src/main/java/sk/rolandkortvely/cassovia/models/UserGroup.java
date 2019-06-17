package sk.rolandkortvely.cassovia.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.SessionFactory;

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

    public UserGroup() {
    }

    public UserGroup(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public static UserGroup find(SessionFactory sessionFactory, Integer id) {
        return query(sessionFactory)
                .where("id", id)
                .stream()
                .findFirst().orElse(null);
    }

    public static List<UserGroup> all(SessionFactory sessionFactory) {
        return query(sessionFactory)
                .stream()
                .collect(Collectors.toList());
    }

    public static Stream<UserGroup> stream(SessionFactory sessionFactory) {
        return stream(UserGroup.class, sessionFactory);
    }

    public static QueryStream<UserGroup> query(SessionFactory sessionFactory) {
        return new QueryStream<>(UserGroup.class, sessionFactory, "groupName");
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
