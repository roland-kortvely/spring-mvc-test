package sk.rolandkortvely.cassovia.models;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Stream;

/**
 * Abstract model to extend Models with useful methods
 */
public abstract class AbstractModel {

    /**
     * Database context (MySQL)
     * DI does not work in new instances, we cannot use @Autowired
     */
    private SessionFactory sessionFactory;

    /**
     * For new empty Models, created by Query
     */
    AbstractModel() {
    }

    /**
     * DI does not work in new instances
     *
     * @param sessionFactory Database context (MySQL)
     */
    AbstractModel(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Find Model by ID
     *
     * @param modelClass     Class of the model we want to find
     * @param sessionFactory Database context (MySQL)
     * @param id             Integer ID of Model we want to find
     * @param <Model>        Any model extending AbstractModel
     * @return model we found in database
     */
    protected static <Model extends AbstractModel> Model find(Class<Model> modelClass, SessionFactory sessionFactory, Integer id) {
        Model result = transaction(session -> modelClass.cast(session.get(modelClass, id)), sessionFactory);

        //Set back sessionFactory to newly created user, we want to be able to delete him or update, ..
        if (result != null) {
            result.setSessionFactory(sessionFactory);
        }

        return result;
    }

    /**
     * @param modelClass     Class of the models we want to find
     * @param sessionFactory Database context (MySQL)
     * @param <Model>        Any model extending AbstractModel
     * @return List of models founded in database
     */
    protected static <Model extends AbstractModel> List<Model> all(Class<Model> modelClass, SessionFactory sessionFactory) {

        List<Model> result = query((session, query) -> query.getResultList(), modelClass, sessionFactory);

        result.forEach(Model -> Model.setSessionFactory(sessionFactory));

        return result;
    }

    /**
     * Useful for testing
     *
     * @param modelClass     Class of the models we want to find
     * @param sessionFactory Database context (MySQL)
     * @param <Model>        Any model extending AbstractModel
     * @return first model we found in database
     */
    protected static <Model extends AbstractModel> Model first(Class<Model> modelClass, SessionFactory sessionFactory) {

        Model result = query((session, query) -> query.getResultList().stream().findFirst().orElse(null), modelClass, sessionFactory);

        //Set back sessionFactory to newly created user, we want to be able to delete him or update, ..
        if (result != null) {
            result.setSessionFactory(sessionFactory);
        }

        return result;
    }

    /**
     * @param modelClass     Class of the models we want to find
     * @param sessionFactory Database context (MySQL)
     * @param <Model>        Any model extending AbstractModel
     * @return Stream of Models from database
     */
    protected static <Model extends AbstractModel> Stream<Model> stream(Class<Model> modelClass, SessionFactory sessionFactory) {
        return query((session, query) -> query.getResultList().stream(), modelClass, sessionFactory);
    }

    /**
     * @param queryTransaction Functional interface, to specify query we want to process
     * @param modelClass       Class of the models we want to find
     * @param sessionFactory   Database context (MySQL)
     * @param <ReturnType>     Anything we want to return from database
     * @param <Model>          Any model extending AbstractModel
     * @return Result of QueryTransaction
     */
    private static <ReturnType, Model> ReturnType query(QueryTransaction<ReturnType, Model> queryTransaction, Class<Model> modelClass, SessionFactory sessionFactory) {
        return transaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Model> cq = cb.createQuery(modelClass);
            Root<Model> root = cq.from(modelClass);
            cq.select(root);
            return queryTransaction.commit(session, session.createQuery(cq));
        }, sessionFactory);
    }

    /**
     * @param sessionTransaction Functional Interface
     * @param sessionFactory     Database context (MySQL)
     * @param <ReturnType>       Type of what we want to return from the database
     * @return Anything we want to return from database
     */
    private static <ReturnType> ReturnType transaction(SessionTransaction<ReturnType> sessionTransaction, @NotNull SessionFactory sessionFactory) {
        Transaction tx = null;
        ReturnType result = null;

        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            result = sessionTransaction.commit(session);
            tx.commit();
        } catch (Exception ReturnType) {
            if (tx != null) tx.rollback();
            ReturnType.printStackTrace();
        }

        return result;
    }

    /**
     * Save current model
     */
    public void save() {
        transaction(session -> {
            session.saveOrUpdate(this);
            return null;
        }, sessionFactory);
    }

    /**
     * Delete current model
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
    protected void setSessionFactory(SessionFactory sessionFactory) {
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

    /**
     * Functional interface, to specify query we want to process
     *
     * @param <ReturnType> Anything we want to return from database
     */
    @FunctionalInterface
    public interface QueryTransaction<ReturnType, Model> {
        ReturnType commit(Session session, Query<Model> query);
    }
}
