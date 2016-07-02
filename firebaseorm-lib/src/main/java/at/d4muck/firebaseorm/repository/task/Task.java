package at.d4muck.firebaseorm.repository.task;

import at.d4muck.firebaseorm.repository.task.callback.OnCompleteListener;

/**
 * @author Christoph Muck
 */
public interface Task<T> {
    boolean isComplete();

    boolean isSuccessful();

    Throwable getException();

    Task<T> addOnCompleteListener(OnCompleteListener<? super T> onSuccessListener);
}
