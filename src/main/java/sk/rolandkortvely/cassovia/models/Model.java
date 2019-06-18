package sk.rolandkortvely.cassovia.models;

import org.hibernate.Session;
import org.hibernate.Transaction;
import sk.rolandkortvely.cassovia.DB;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract Model to extend Models with useful methods
 */
public abstract class Model<AModel extends Model<AModel>> {

    private final Class<AModel> modelClass;

    Model(Class<AModel> modelClass) {
        this.modelClass = modelClass;
    }

    public AModel find(Integer id) {
        return query()
                .where("id", id)
                .stream()
                .findFirst().orElse(null);
    }

    public List<AModel> all() {
        return query()
                .stream()
                .collect(Collectors.toList());
    }

    public QueryStream<AModel> where(String s, Object c) {
        return (new QueryStream<>(getModelClass(), DB.sessionFactory)).where(s, c);
    }

    public Stream<AModel> stream() {
        return new QueryStream<>(getModelClass(), DB.sessionFactory).stream();
    }

    public QueryStream<AModel> query() {
        return new QueryStream<>(getModelClass(), DB.sessionFactory);
    }

    private static <ReturnType> void transaction(SessionTransaction<ReturnType> sessionTransaction) {
        Transaction tx = null;

        try (Session session = DB.sessionFactory.openSession()) {
            tx = session.beginTransaction();
            sessionTransaction.commit(session);
            tx.commit();
        } catch (Exception ReturnType) {
            if (tx != null) tx.rollback();
            ReturnType.printStackTrace();
        }
    }

    /**
     * Save current AModel
     */
    public void save() {
        transaction(session -> {
            session.saveOrUpdate(this);
            return null;
        });
    }

    /**
     * Delete current AModel
     */
    public void delete() {
        transaction(session -> {
            session.remove(this);
            return null;
        });
    }

    /**
     * Functional interface, to specify tasks for database session
     *
     * @param <ReturnType> Anything we want to return from database
     */
    @FunctionalInterface
    public interface SessionTransaction<ReturnType> {
        ReturnType commit(Session session);
    }

    private Class<AModel> getModelClass() {
        return modelClass;
    }
}
