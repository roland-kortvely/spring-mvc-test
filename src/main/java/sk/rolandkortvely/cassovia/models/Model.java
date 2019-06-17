package sk.rolandkortvely.cassovia.models;

import org.hibernate.Session;
import org.hibernate.Transaction;
import sk.rolandkortvely.cassovia.DB;

import java.util.stream.Stream;

/**
 * Abstract Model to extend Models with useful methods
 */
public abstract class Model {

    /**
     * Database context (MySQL)
     * DI does not work in new instances, we cannot use @Autowired
     */

    protected static <AModel extends Model> Stream<AModel> stream(Class<AModel> modelClass) {
        return new QueryStream<>(modelClass, DB.sessionFactory).stream();
    }

    /**
     * @param sessionTransaction Functional Interface
     * @param <ReturnType>       Type of what we want to return from the database
     */
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
}
