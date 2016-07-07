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

package at.d4muck.fireworm.reflection.read;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.d4muck.fireworm.reflection.model.ReflectiveModel;
import at.d4muck.fireworm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
class CollectionReferenceReader implements ReferenceReader {

    private final ReflectiveModelFactory reflectiveModelFactory;

    private final ReflectiveModel modelResolver;
    private final Field field;
    private Set<ReflectiveModel> referenceReflectiveModels;

    CollectionReferenceReader(ReflectiveModel model, Field field, ReflectiveModelFactory reflectiveModelFactory) {
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

        List<String> ids = (List<String>) references;

        ParameterizedType integerListType = (ParameterizedType) field.getGenericType();
        Class<?> collectionType = (Class<?>) integerListType.getActualTypeArguments()[0];
        Collection referenceModels = getReferenceModelCollection();
        for (String id : ids) {
            Object referenceModel;
            try {
                referenceModel = collectionType.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            ReflectiveModel referenceReflectiveModel = reflectiveModelFactory.newReflectiveModelOf(referenceModel);

            referenceReflectiveModel.resolveFieldDeclarations();
            referenceReflectiveModel.resolveId();
            referenceReflectiveModel.setId(id);

            referenceModels.add(referenceModel);
            referenceReflectiveModels.add(referenceReflectiveModel);
        }
    }

    private Collection getReferenceModelCollection() {
        try {
            return (Collection) field.get(modelResolver.getModel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
