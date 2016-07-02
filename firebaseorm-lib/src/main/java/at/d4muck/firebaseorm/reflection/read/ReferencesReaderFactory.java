package at.d4muck.firebaseorm.reflection.read;

import javax.inject.Inject;
import javax.inject.Singleton;

import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
@Singleton
public class ReferencesReaderFactory {

    private final ReflectiveModelFactory reflectiveModelFactory;

    @Inject
    public ReferencesReaderFactory(ReflectiveModelFactory reflectiveModelFactory) {
        this.reflectiveModelFactory = reflectiveModelFactory;
    }

    public ReferencesReader newReferencesReader(ReflectiveModel reflectiveModel) {
        return new ReferencesReader(reflectiveModel, reflectiveModelFactory);
    }
}
