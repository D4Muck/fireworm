package at.d4muck.firebaseorm.reflection.write;

import javax.inject.Inject;
import javax.inject.Singleton;

import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
@Singleton
public class ReferencesResolverFactory {

    private final ReflectiveModelFactory reflectiveModelFactory;

    @Inject
    public ReferencesResolverFactory(ReflectiveModelFactory reflectiveModelFactory) {
        this.reflectiveModelFactory = reflectiveModelFactory;
    }

    public ReferencesResolver newReferencesResolver(ReflectiveModel reflectiveModel) {
        return new ReferencesResolver(reflectiveModel, reflectiveModelFactory);
    }
}
