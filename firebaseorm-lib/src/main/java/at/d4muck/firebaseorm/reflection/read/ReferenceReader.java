package at.d4muck.firebaseorm.reflection.read;

import java.util.Set;

import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;

/**
 * @author Christoph Muck
 */
interface ReferenceReader {
    Set<ReflectiveModel> getReferenceReflectiveModels();

    void readReferences(Object references);
}
