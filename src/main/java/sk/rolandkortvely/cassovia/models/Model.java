package sk.rolandkortvely.cassovia.models;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Abstract Model to extend Models with useful methods
 */
public abstract class Model {

    /**
     * Database context (MySQL)
     * DI does not work in new instances, we cannot use @Autowired
     */
    protected SessionFactory sessionFactory;

    /**
     * For new empty Models, created by Query
     */
    Model() {
    }

    /**
     * DI does not work in new instances
     *
     * @param sessionFactory Database context (MySQL)
     */
    Model(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected static <AModel extends Model> Stream<AModel> stream(Class<AModel> modelClass, SessionFactory sessionFactory) {
        return new QueryStream<>(modelClass, sessionFactory).stream();
    }

    /**
     * @param sessionTransaction Functional Interface
     * @param sessionFactory     Database context (MySQL)
     * @param <ReturnType>       Type of what we want to return from the database
     */
    private static <ReturnType> void transaction(SessionTransaction<ReturnType> sessionTransaction, @NotNull SessionFactory sessionFactory) {
        Transaction tx = null;

        try (Session session = sessionFactory.openSession()) {
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
        }, sessionFactory);
    }

    /**
     * Delete current AModel
     */
    public void delete() {
        transaction(session -> {
            session.remove(this);
            return null;
        }, sessionFactory);
    }

    /**
     * Used to refresh database context in Models we will receive from database
     *
     * @param sessionFactory Database context (MySQL)
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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
}
