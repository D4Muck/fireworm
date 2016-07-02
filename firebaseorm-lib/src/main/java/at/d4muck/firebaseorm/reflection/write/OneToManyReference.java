package at.d4muck.firebaseorm.reflection.write;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import at.d4muck.firebaseorm.reflection.annotation.ManyToOne;
import at.d4muck.firebaseorm.reflection.annotation.OneToMany;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
class OneToManyReference implements Reference {

    private final ReflectiveModelFactory reflectiveModelFactory;
    private final OneToMany oneToMany;

    private final Field field;
    private final ReflectiveModel reflectiveModel;

    private Map<ReflectiveModel, ManyToOneReference> references = new HashMap<>();

    OneToManyReference(Field field, ReflectiveModel reflectiveModel,
                       OneToMany oneToMany, ReflectiveModelFactory reflectiveModelFactory) {
        this.field = field;
        this.reflectiveModel = reflectiveModel;
        this.oneToMany = oneToMany;
        this.reflectiveModelFactory = reflectiveModelFactory;
    }

    @Override
    public void resolve() {
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection referenceModels = getReferenceModels();
            for (Object referenceModel : referenceModels) {
                ReflectiveModel referenceReflectiveModel = reflectiveModelFactory.newReflectiveModelOf(referenceModel);
                referenceReflectiveModel.resolveAll();

                ManyToOneReference backReference = getBackReferenceOf(referenceReflectiveModel);
                references.put(referenceReflectiveModel, backReference);
            }
        } else {
            throw new UnsupportedOperationException("Only collections are allowed as references at this time!");
        }
    }

    Collection getReferenceModels() {
        try {
            return (Collection) field.get(reflectiveModel.getModel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ManyToOneReference getBackReferenceOf(ReflectiveModel reflectiveModel) {
        for (Map.Entry<Field, Annotation> entry : reflectiveModel.getReferencedFields().entrySet()) {
            Annotation referenceType = entry.getValue();
            if (referenceType instanceof ManyToOne) {
                ManyToOne referenceManyToMany = (ManyToOne) referenceType;
                if (referenceManyToMany.mappedBy().equals(oneToMany.mappedBy())) {
                    return new ManyToOneReference(entry.getKey(), reflectiveModel, referenceManyToMany, reflectiveModelFactory);
                }
            }
        }
        return null;
    }

    @Override
    public Collection<? extends ReflectiveModel> getReferences() {
        return references.keySet();
    }

    @Override
    public void putReferenceIds() {
        reflectiveModel.putInFieldMap(field.getName(), getReferenceIds());
    }

    private List<String> getReferenceIds() {
        List<String> keys = new LinkedList<>();
        for (ReflectiveModel resolver : references.keySet()) {
            resolver.resolveId();
            keys.add(resolver.getId());
        }
        return keys;
    }

    @Override
    public void inflateReferences() {
        for (ManyToOneReference backReference : references.values()) {
            if (backReference != null) {
                backReference.setReferenceModelObject(reflectiveModel);
                backReference.putReferenceIds();
            }
        }
    }

    @Override
    public Map<String, Object> generateChildUpdates(String otherId) {
        return Collections.emptyMap();
    }
}
