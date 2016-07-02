/*
 * Copyright 2016 Christoph Muck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.d4muck.firebaseorm.reflection.write;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
public class ReferencesResolver {

    private final ReflectiveModelFactory reflectiveModelFactory;

    private final ReflectiveModel modelResolver;
    private Set<Reference> references;

    ReferencesResolver(ReflectiveModel model, ReflectiveModelFactory reflectiveModelFactory) {
        this.modelResolver = model;
        this.reflectiveModelFactory = reflectiveModelFactory;
    }

    public boolean hasReferences() {
        return !references.isEmpty();
    }

    public void resolveReferences() {
        references = new HashSet<>();
        Map<Field, Annotation> dependantFields = modelResolver.getReferencedFields();
        for (Field field : dependantFields.keySet()) {
            Annotation referenceType = dependantFields.get(field);
            Reference reference;
            if (referenceType instanceof ManyToMany) {
                reference = new ManyToManyReference(field, modelResolver, (ManyToMany) referenceType, reflectiveModelFactory);
            } else if (referenceType instanceof OneToMany) {
                reference = new OneToManyReference(field, modelResolver, (OneToMany) referenceType, reflectiveModelFactory);
            } else if (referenceType instanceof ManyToOne) {
                reference = new ManyToOneReference(field, modelResolver, (ManyToOne) referenceType, reflectiveModelFactory);
            } else {
                throw new IllegalArgumentException("Unsupported reference type: " + referenceType.getClass().getCanonicalName());
            }

            references.add(reference);
            reference.resolve();
        }
    }

    public List<ReflectiveModel> getReferenceModels() {
        List<ReflectiveModel> allList = new LinkedList<>();
        for (Reference reference : references) {
            allList.addAll(reference.getReferences());
        }
        return allList;
    }

    public void generateReferenceIds() {
        for (Reference reference : references) {
            reference.putReferenceIds();
        }
    }

    public void inflateReferences() {
        for (Reference reference : references) {
            reference.inflateReferences();
        }
    }

    public Map<String, Object> generateChildUpdates() {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Reference reference : references) {
            Map<String, Object> referenceChildUpdates = reference.generateChildUpdates(modelResolver.getId());
            childUpdates.putAll(referenceChildUpdates);
        }
        return childUpdates;
    }
}
