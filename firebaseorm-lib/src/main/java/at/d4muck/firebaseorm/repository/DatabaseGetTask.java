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

package at.d4muck.firebaseorm.repository;

import java.util.LinkedList;
import java.util.List;

import at.d4muck.firebaseorm.repository.task.Task;
import at.d4muck.firebaseorm.repository.task.callback.OnCompleteListener;

/**
 * @author Christoph Muck
 */
public class DatabaseGetTask<T> implements Task<T> {

    private T result;
    private Throwable e;

    private List<OnCompleteListener<? super T>> completeListeners = new LinkedList<>();

    @Override
    public boolean isComplete() {
        return result != null;
    }

    @Override
    public boolean isSuccessful() {
        return e == null;
    }

    void setResult(T result) {
        this.result = result;
        notifyListeners();
    }

    private void notifyListeners() {
        for (OnCompleteListener<? super T> listener : completeListeners) {
            if (isSuccessful()) {
                listener.onSuccess(result);
            } else {
                listener.onFailure(e);
            }
        }
    }

    @Override
    public Throwable getException() {
        return e;
    }

    void setException(Throwable e) {
        this.e = e;
        notifyListeners();
    }

    @Override
    public Task<T> addOnCompleteListener(OnCompleteListener<? super T> onSuccessListener) {
        if (onSuccessListener != null) {
            completeListeners.add(onSuccessListener);
        }
        return this;
    }
}
