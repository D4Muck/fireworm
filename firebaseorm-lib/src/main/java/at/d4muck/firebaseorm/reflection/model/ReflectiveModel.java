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

package at.d4muck.firebaseorm.reflection.model;

import com.google.common.base.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.d4muck.firebaseorm.reflection.annotation.Id;
import at.d4muck.firebaseorm.reflection.annotation.ManyToMany;
import at.d4muck.firebaseorm.reflection.annotation.ManyToOne;
import at.d4muck.firebaseorm.reflection.annotation.OneToMany;
import at.d4muck.firebaseorm.reflection.util.ObjectConverter;

/**
 * @author Christoph Muck
 */
public class ReflectiveModel {

    private final Object model;
    private final ObjectConverter converter;

    private Field idField;
    private Map<String, Field> fields;
    private Map<Field, Annotation> referencedFields = new HashMap<>();

    private String id;
    private Map<String, Object> fieldMap;

    ReflectiveModel(Object model, ObjectConverter converter) {
        this.model = model;
        this.converter = converter;
    }

    public void resolveAll() {
        resolveFieldDeclarations();
        resolveId();
        resolveFieldValues();
    }

    public void resolveFieldDeclarations() {
        fields = new HashMap<>();
        Field[] fields = model.getClass().getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (fieldName.startsWith("$")) continue;

            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                this.referencedFields.put(field, manyToMany);
            } else if (field.isAnnotationPresent(OneToMany.class)) {
                OneToMany annotation = field.getAnnotation(OneToMany.class);
                this.referencedFields.put(field, annotation);
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne annotation = field.getAnnotation(ManyToOne.class);
                this.referencedFields.put(field, annotation);
            } else {
                if (field.isAnnotationPresent(Id.class)) {
                    idField = field;
                } else {
                    this.fields.put(fieldName, field);
                }
            }
        }
    }

    public void resolveId() {
        checkIdFieldPresent();
        id = getNullableStringOf(idField);
    }

    public void resolveFieldValues() {
        fieldMap = new HashMap<>();
        for (Field field : fields.values()) {
            fieldMap.put(field.getName(), getStringOf(field));
        }
    }

    private void checkIdFieldPresent() {
        checkFieldsResolved();
        Preconditions.checkState(idField != null, "The model %s has no idField", this);
    }

    private String getNullableStringOf(Field field) {
        Object value = getObjectOf(field);
        return value != null ? value.toString() : null;
    }

    private String getStringOf(Field field) {
        Object value = getObjectOf(field);
        return value != null ? value.toString() : "";
    }

    private void checkFieldsResolved() {
        Preconditions.checkState(fields != null, "Fields of model %s not resolved", this);
    }

    private Object getObjectOf(Field field) {
        try {
            return field.get(model);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        checkIdFieldPresent();
        setModelField(idField, id);
        this.id = id;
    }

    private void setModelField(Field field, Object value) {
        try {
            field.set(model, converter.cast(value, field.getType()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFieldsIfPresent(HashMap<String, Object> fieldMap) {
        checkFieldsResolved();
        for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
            Field field = fields.get(entry.getKey());
            setFieldIfPresent(entry.getValue(), field);
        }
    }

    private void setFieldIfPresent(Object value, Field field) {
        if (field != null) {
            fieldMap.put(field.getName(), value);
            setModelField(field, value);
        }
    }

    public Map<String, Object> getFieldMap() {
        checkFieldValuesResolved();
        return Collections.unmodifiableMap(fieldMap);
    }

    private void checkFieldValuesResolved() {
        checkFieldsResolved();
        Preconditions.checkState(fieldMap != null, "Field values of model %s not resolved", this);
    }

    public String getCamelCasePluralClassNameOfModel() {
        String className = model.getClass().getSimpleName();
        String withoutFirst = className.substring(1);
        String first = className.substring(0, 1);
        return first.toLowerCase() + withoutFirst + "s";
    }

    public Object getModel() {
        return model;
    }

    public void putInFieldMap(String key, List<String> value) {
        this.fieldMap.put(key, value);
    }

    public void putInFieldMap(String key, String value) {
        this.fieldMap.put(key, value);
    }

    public Field getIdField() {
        return idField;
    }

    public Map<Field, Annotation> getReferencedFields() {
        return referencedFields;
    }
}