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

package at.d4muck.fireworm;

import java.util.Set;

import at.d4muck.fireworm.DaggerAppComponent;
import at.d4muck.fireworm.repository.Database;
import at.d4muck.fireworm.repository.task.Task;

/**
 * @author Christoph Muck
 */
public abstract class Repository<T> {

    Database database = DaggerAppComponent.create().database();

    private final Class<T> modelClass;

    public Repository(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    public void set(T entity) {
        database.set(entity);
    }

    public Task<T> get(String id) {
        return database.get(id, modelClass);
    }

    public Task<Set<T>> getAll() {
        return database.getAll(modelClass);
    }
}
