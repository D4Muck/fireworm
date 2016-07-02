package at.d4muck.firebaseorm.reflection.model;

import javax.inject.Inject;
import javax.inject.Singleton;

import at.d4muck.firebaseorm.reflection.util.ObjectConverter;

/**
 * @author Christoph Muck
 */
@Singleton
public class ReflectiveModelFactory {

    private final ObjectConverter converter;

    @Inject
    ReflectiveModelFactory(ObjectConverter converterProvider) {
        this.converter = converterProvider;
    }

    public ReflectiveModel newReflectiveModelOf(Object model) {
        return new ReflectiveModel(model, converter);
    }
}
