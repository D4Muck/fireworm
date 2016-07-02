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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import at.d4muck.firebaseorm.reflection.annotation.ManyToOne;
import at.d4muck.firebaseorm.reflection.annotation.OneToMany;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;

/**
 * @author Christoph Muck
 */
class ManyToOneReference implements Reference {

    private final ReflectiveModelFactory reflectiveModelFactory;
    private final ManyToOne manyToOne;

    private final Field field;

    private ReflectiveModel reflectiveModel;
    private ReflectiveModel referenceReflectiveModel;
    private OneToManyReference backReference;

    ManyToOneReference(Field field, ReflectiveModel reflectiveModel,
                       ManyToOne manyToOne, ReflectiveModelFactory reflectiveModelFactory) {
        this.field = field;
        this.reflectiveModel = reflectiveModel;
        this.manyToOne = manyToOne;
        this.reflectiveModelFactory = reflectiveModelFactory;
    }


    @Override
    public void resolve() {
        Object referenceModel = getReferenceModel();
        if (referenceModel != null) {
            referenceReflectiveModel = reflectiveModelFactory.newReflectiveModelOf(referenceModel);
            referenceReflectiveModel.resolveAll();

            backReference = getBackReferenceOf(referenceReflectiveModel);
        }
    }

    private Object getReferenceModel() {
        try {
            return field.get(reflectiveModel.getModel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    void setReferenceModelObject(ReflectiveModel referenceReflectiveModel) {
        try {
            field.set(reflectiveModel.getModel(), referenceReflectiveModel.getModel());
            this.referenceReflectiveModel = referenceReflectiveModel;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private OneToManyReference getBackReferenceOf(ReflectiveModel reflectiveModel) {
        for (Map.Entry<Field, Annotation> entry : reflectiveModel.getReferencedFields().entrySet()) {
            Annotation referenceType = entry.getValue();
            if (referenceType instanceof OneToMany) {
                OneToMany oneToManyReference = (OneToMany) referenceType;
                if (oneToManyReference.mappedBy().equals(manyToOne.mappedBy())) {
                    return new OneToManyReference(entry.getKey(), reflectiveModel, oneToManyReference, reflectiveModelFactory);
                }
            }
        }
        return null;
    }

    @Override
    public Collection<? extends ReflectiveModel> getReferences() {
        return Collections.singleton(referenceReflectiveModel);
    }

    @Override
    public void putReferenceIds() {
        if (referenceReflectiveModel != null) {
            referenceReflectiveModel.resolveId();
            reflectiveModel.putInFieldMap(field.getName(), referenceReflectiveModel.getId());
        }
    }

    @Override
    public void inflateReferences() {
        if (backReference != null) {
            Collection collection = backReference.getReferenceModels();

            if (!collection.contains(reflectiveModel.getModel())) {
                collection.add(reflectiveModel.getModel());
            }

            backReference.putReferenceIds();
        }
    }

    @Override
    public Map<String, Object> generateChildUpdates(String otherId) {
        return Collections.emptyMap();
    }
}
