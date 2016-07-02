package at.d4muck.firebaseorm.reflection.write;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.d4muck.firebaseorm.reflection.annotation.ManyToMany;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
class ManyToManyReference implements Reference {

    private final ReflectiveModelFactory reflectiveModelFactory;
    private final ManyToMany manyToMany;

    private final Field field;
    private final ReflectiveModel reflectiveModel;

    private Map<ReflectiveModel, ManyToManyReference> references = new HashMap<>();

    ManyToManyReference(Field field, ReflectiveModel reflectiveModel,
                        ManyToMany manyToMany, ReflectiveModelFactory reflectiveModelFactory) {
        this.field = field;
        this.reflectiveModel = reflectiveModel;
        this.manyToMany = manyToMany;
        this.reflectiveModelFactory = reflectiveModelFactory;
    }

    public void resolve() {
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection referenceModels = getReferenceModels();
            for (Object referenceModel : referenceModels) {
                ReflectiveModel referenceReflectiveModel = reflectiveModelFactory.newReflectiveModelOf(referenceModel);
                referenceReflectiveModel.resolveAll();

                ManyToManyReference backReference = getBackReferenceOf(referenceReflectiveModel);
                references.put(referenceReflectiveModel, backReference);
            }
        } else {
            throw new UnsupportedOperationException("Only collections are allowed as references at this time!");
        }
    }

    private Collection getReferenceModels() {
        try {
            return (Collection) field.get(reflectiveModel.getModel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ManyToManyReference getBackReferenceOf(ReflectiveModel reflectiveModel) {
        for (Map.Entry<Field, Annotation> entry : reflectiveModel.getReferencedFields().entrySet()) {
            Annotation referenceType = entry.getValue();
            if (referenceType instanceof ManyToMany) {
                ManyToMany referenceManyToMany = (ManyToMany) referenceType;
                if (referenceManyToMany.mappedBy().equals(manyToMany.mappedBy())) {
                    return new ManyToManyReference(entry.getKey(), reflectiveModel, referenceManyToMany, reflectiveModelFactory);
                }
            }
        }
        return null;
    }

    public Set<ReflectiveModel> getReferences() {
        return references.keySet();
    }

    public void inflateReferences() {
        for (ManyToManyReference backReference : references.values()) {
            if (backReference != null) {
                Collection collection = backReference.getReferenceModels();

                if (!collection.contains(reflectiveModel.getModel())) {
                    collection.add(reflectiveModel.getModel());
                }

                backReference.putReferenceIds();
            }
        }
    }

    public void putReferenceIds() {
        if (isRedundant()) {
            reflectiveModel.putInFieldMap(field.getName(), getReferenceIds());
        }
    }

    private boolean isRedundant() {
        return manyToMany.redundant();
    }

    private List<String> getReferenceIds() {
        List<String> keys = new LinkedList<>();
        for (ReflectiveModel resolver : references.keySet()) {
            resolver.resolveId();
            keys.add(resolver.getId());
        }
        return keys;
    }

    public Map<String, Object> generateChildUpdates(String otherId) {
        Map<String, Object> childUpdates = new HashMap<>();
        if (manyToMany.reverse()) {
            for (ReflectiveModel reference : references.keySet()) {
                childUpdates.put(getMappedBy() + "/" + reference.getId() + "/" + otherId, "");
            }
        } else {
            for (ReflectiveModel reference : references.keySet()) {
                childUpdates.put(getMappedBy() + "/" + otherId + "/" + reference.getId(), "");
            }
        }
        return childUpdates;
    }

    private String getMappedBy() {
        return manyToMany.mappedBy();
    }
}
