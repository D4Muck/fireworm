package at.d4muck.firebaseorm.reflection.write;

import java.util.Collection;
import java.util.Map;

import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;

/**
 * @author Christoph Muck
 */
public interface Reference {
    void resolve();

    Collection<? extends ReflectiveModel> getReferences();

    void putReferenceIds();

    void inflateReferences();

    Map<String,Object> generateChildUpdates(String id);
}
