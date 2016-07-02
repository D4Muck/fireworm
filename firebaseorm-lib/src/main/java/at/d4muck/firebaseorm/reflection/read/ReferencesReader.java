package at.d4muck.firebaseorm.reflection.read;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.d4muck.firebaseorm.reflection.annotation.ManyToMany;
import at.d4muck.firebaseorm.reflection.annotation.ManyToOne;
import at.d4muck.firebaseorm.reflection.annotation.OneToMany;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
public class ReferencesReader {

    private final ReflectiveModelFactory reflectiveModelFactory;

    private final ReflectiveModel modelResolver;
    private Map<Field, ReferenceReader> referenceReaderMap;

    ReferencesReader(ReflectiveModel model, ReflectiveModelFactory reflectiveModelFactory) {
        this.modelResolver = model;
        this.reflectiveModelFactory = reflectiveModelFactory;
    }

    public void readReferences(Map<String, Object> fieldMap) {
        referenceReaderMap = new HashMap<>();
        for (Map.Entry<Field, Annotation> entry : modelResolver.getReferencedFields().entrySet()) {
            Field field = entry.getKey();
            Annotation annotation = entry.getValue();

            ReferenceReader referenceReader;
            if (annotation instanceof ManyToMany || annotation instanceof OneToMany) {
                referenceReader = new CollectionReferenceReader(modelResolver, field, reflectiveModelFactory);
            } else if (annotation instanceof ManyToOne) {
                referenceReader = new InstanceReferenceReader(modelResolver, field, reflectiveModelFactory);
            } else {
                throw new IllegalArgumentException("Unsupported Reference Type :" + annotation.getClass().getCanonicalName());
            }

            referenceReader.readReferences(fieldMap.get(field.getName()));
            referenceReaderMap.put(field, referenceReader);
        }
    }

    public Set<ReflectiveModel> getReferenceReflectiveModels() {
        Set<ReflectiveModel> reflectiveModels = new HashSet<>();
        for (ReferenceReader referenceReader : referenceReaderMap.values()) {
            reflectiveModels.addAll(referenceReader.getReferenceReflectiveModels());
        }
        return reflectiveModels;
    }
}
