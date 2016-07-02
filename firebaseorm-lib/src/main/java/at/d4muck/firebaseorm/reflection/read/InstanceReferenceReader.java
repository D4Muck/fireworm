package at.d4muck.firebaseorm.reflection.read;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
class InstanceReferenceReader implements ReferenceReader {

    private final ReflectiveModelFactory reflectiveModelFactory;

    private final ReflectiveModel modelResolver;
    private final Field field;
    private Set<ReflectiveModel> referenceReflectiveModels;

    InstanceReferenceReader(ReflectiveModel model, Field field, ReflectiveModelFactory reflectiveModelFactory) {
        this.modelResolver = model;
        this.field = field;
        this.reflectiveModelFactory = reflectiveModelFactory;
    }

    @Override
    public Set<ReflectiveModel> getReferenceReflectiveModels() {
        return referenceReflectiveModels;
    }

    @Override
    public void readReferences(Object references) {
        referenceReflectiveModels = new HashSet<>();
        if (references == null) return;

        String id = (String) references;
        Class<?> type = field.getType();

        Object referenceModel;
        try {
            referenceModel = type.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        ReflectiveModel referenceReflectiveModel = reflectiveModelFactory.newReflectiveModelOf(referenceModel);

        referenceReflectiveModel.resolveFieldDeclarations();
        referenceReflectiveModel.resolveId();
        referenceReflectiveModel.setId(id);

        setReferenceModel(referenceModel);

        referenceReflectiveModels = Collections.singleton(referenceReflectiveModel);
    }

    private void setReferenceModel(Object model) {
        try {
            field.set(modelResolver.getModel(), model);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
