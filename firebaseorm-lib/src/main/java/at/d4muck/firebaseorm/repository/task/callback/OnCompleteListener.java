package at.d4muck.firebaseorm.repository.task.callback;

/**
 * @author Christoph Muck
 */
public interface OnCompleteListener<T> {

    void onSuccess(T result);

    void onFailure(Throwable e);
}
