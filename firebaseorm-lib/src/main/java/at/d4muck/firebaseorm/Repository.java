package at.d4muck.firebaseorm;

import java.util.Set;

import javax.inject.Inject;

import at.d4muck.firebaseorm.DaggerAppComponent;
import at.d4muck.firebaseorm.repository.Database;
import at.d4muck.firebaseorm.repository.task.Task;

/**
 * @author Christoph Muck
 */
public abstract class Repository<T> {

    Database database = DaggerAppComponent.create().database();

    private final Class<T> modelClass;

    public Repository(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    public void set(T entity) {
        database.set(entity);
    }

    public Task<T> get(String id) {
        return database.get(id, modelClass);
    }

    public Task<Set<T>> getAll() {
        return database.getAll(modelClass);
    }
}
