package at.d4muck.firebaseorm.repository;

import android.os.Handler;
import android.os.Looper;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import at.d4muck.firebaseorm.reflection.model.ReflectiveModel;
import at.d4muck.firebaseorm.reflection.model.ReflectiveModelFactory;
import at.d4muck.firebaseorm.reflection.read.ReferencesReader;
import at.d4muck.firebaseorm.reflection.read.ReferencesReaderFactory;
import at.d4muck.firebaseorm.reflection.write.ReferencesResolver;
import at.d4muck.firebaseorm.reflection.write.ReferencesResolverFactory;
import at.d4muck.firebaseorm.repository.task.Task;

/**
 * @author Christoph Muck
 */
@Singleton
public class Database {

    private final Executor executor = Executors.newCachedThreadPool();
    private final FirebaseDatabase database;
    private final ReflectiveModelFactory reflectiveModelFactory;
    private final ReferencesReaderFactory referencesReaderFactory;
    private final ReferencesResolverFactory referencesResolverFactory;

    @Inject
    public Database(FirebaseDatabase firebaseDatabase,
                    ReflectiveModelFactory reflectiveModelFactory,
                    ReferencesReaderFactory referencesReaderFactory,
                    ReferencesResolverFactory referencesResolverFactory) {
        this.database = firebaseDatabase;
        this.reflectiveModelFactory = reflectiveModelFactory;
        this.referencesReaderFactory = referencesReaderFactory;
        this.referencesResolverFactory = referencesResolverFactory;
    }

    public <T> void set(T entity) {
        ReflectiveModel model = reflectiveModelFactory.newReflectiveModelOf(entity);
        model.resolveAll();

        ReferencesResolver referencesResolver = referencesResolverFactory.newReferencesResolver(model);
        referencesResolver.resolveReferences();
        if (referencesResolver.hasReferences()) {

            persist(referencesResolver.getReferenceModels());
            //If any id was null, now it has been generated and set in the model

            referencesResolver.generateReferenceIds();
        }

        persist(model);
        model.resolveId();

        if (referencesResolver.hasReferences()) {
            referencesResolver.inflateReferences();
            persist(referencesResolver.getReferenceModels());
            Map<String, Object> childUpdates = referencesResolver.generateChildUpdates();
            childUpdate(childUpdates);
        }
    }

    private void childUpdate(Map<String, Object> childUpdates) {
        DatabaseReference reference = database.getReference();
        reference.updateChildren(childUpdates);
    }

    private void persist(List<ReflectiveModel> entity) {
        for (ReflectiveModel resolver : entity) {
            ReferencesResolver referencesResolver = referencesResolverFactory.newReferencesResolver(resolver);
            referencesResolver.resolveReferences();
            referencesResolver.generateReferenceIds();

            persist(resolver);
        }
    }

    private void persist(ReflectiveModel resolver) {
        DatabaseReference reference = database.getReference(resolver.getCamelCasePluralClassNameOfModel());
        String key = resolver.getId();
        if (key == null) {
            key = reference.push().getKey();
            resolver.setId(key);
        }
        reference.child(key).setValue(resolver.getFieldMap());
    }

    public <T> Task<Set<T>> getAll(final Class<T> modelClass) {
        ListenableFutureTask<Set<T>> futureTask = ListenableFutureTask.create(new DatabaseMultipleGetCallable<T>(modelClass));
        executor.execute(futureTask);
        final DatabaseGetTask<Set<T>> databaseGetTask = new DatabaseGetTask<>();
        Futures.addCallback(futureTask, new FutureCallback<Set<T>>() {
            @Override
            public void onSuccess(Set<T> result) {
                databaseGetTask.setResult(result);
            }

            @Override
            public void onFailure(Throwable t) {
                databaseGetTask.setException(t);
            }
        }, mainLooperExecutor);
        return databaseGetTask;
    }

    private <T> T readModel(String id, HashMap<String, Object> fieldMap, Class<T> modelClass) throws Exception {
        final CountDownLatch[] readCountDownLatch = new CountDownLatch[1];
        final Exception[] e = new Exception[1];

        final T[] model = (T[]) new Object[]{newInstanceOf(modelClass)};
        final ReflectiveModel reflectiveModel = reflectiveModelFactory.newReflectiveModelOf(model[0]);
        reflectiveModel.resolveAll();

        if (fieldMap == null) {
            //NOT FOUND
            model[0] = null;
        } else {
            reflectiveModel.setFieldsIfPresent(fieldMap);
            reflectiveModel.setId(id);

            ReferencesReader referencesReader = referencesReaderFactory.newReferencesReader(reflectiveModel);
            referencesReader.readReferences(fieldMap);

            Set<ReflectiveModel> referenceReflectiveModels = referencesReader.getReferenceReflectiveModels();
            readCountDownLatch[0] = new CountDownLatch(referenceReflectiveModels.size());

            for (final Iterator<ReflectiveModel> referenceReflectiveModelIterator = referencesReader.getReferenceReflectiveModels().iterator(); referenceReflectiveModelIterator.hasNext(); ) {
                final ReflectiveModel referenceReflectiveModel = referenceReflectiveModelIterator.next();
                final DatabaseReference reference = database.getReference(referenceReflectiveModel.getCamelCasePluralClassNameOfModel() + "/" + referenceReflectiveModel.getId());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Object> fieldMap = (HashMap<String, Object>) dataSnapshot.getValue();
                        referenceReflectiveModel.resolveFieldValues();
                        referenceReflectiveModel.setFieldsIfPresent(fieldMap);

                        ReferencesReader referencesReader = referencesReaderFactory.newReferencesReader(referenceReflectiveModel);
                        referencesReader.readReferences(fieldMap);

                        readCountDownLatch[0].countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e[0] = databaseError.toException();
                        readCountDownLatch[0].countDown();
                    }
                });
            }
        }

        if (readCountDownLatch[0] != null) readCountDownLatch[0].await();
        if (e[0] != null) {
            throw e[0];
        }
        return model[0];
    }

    public abstract class DatabaseGetCallable<T> implements Callable<T> {

        private final String databaseReference;

        public DatabaseGetCallable(String databaseReference) {
            this.databaseReference = databaseReference;
        }

        public abstract T convertFromDataSnapshotToModel(DataSnapshot dataSnapshot) throws Exception;

        @Override
        public T call() throws Exception {
            final Exception[] e = new Exception[1];

            final CountDownLatch mainReadLatch = new CountDownLatch(1);
            final DatabaseReference reference = database.getReference(databaseReference);

            final T[] model = (T[]) new Object[1];

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                model[0] = convertFromDataSnapshotToModel(dataSnapshot);
                            } catch (Exception exception) {
                                e[0] = exception;
                            } finally {
                                mainReadLatch.countDown();
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    e[0] = databaseError.toException();
                    mainReadLatch.countDown();
                }
            });
            mainReadLatch.await();
            if (e[0] != null) {
                throw e[0];
            }
            return model[0];
        }
    }

    public class DatabaseSingleGetCallable<T> extends DatabaseGetCallable<T> {
        private final String id;
        private final Class<T> modelClass;

        public DatabaseSingleGetCallable(String id, Class<T> modelClass) {
            super(reflectiveModelFactory.newReflectiveModelOf(newInstanceOf(modelClass)).getCamelCasePluralClassNameOfModel() + "/" + id);
            this.id = id;
            this.modelClass = modelClass;
        }

        @Override
        public T convertFromDataSnapshotToModel(DataSnapshot dataSnapshot) throws Exception {
            HashMap<String, Object> fieldMap = (HashMap<String, Object>) dataSnapshot.getValue();
            return readModel(id, fieldMap, modelClass);
        }
    }

    public class DatabaseMultipleGetCallable<T> extends DatabaseGetCallable<Set<T>> {

        private final Class<T> modelClass;

        public DatabaseMultipleGetCallable(Class<T> modelClass) {
            super(reflectiveModelFactory.newReflectiveModelOf(newInstanceOf(modelClass)).getCamelCasePluralClassNameOfModel());
            this.modelClass = modelClass;
        }

        @Override
        public Set<T> convertFromDataSnapshotToModel(DataSnapshot dataSnapshot) throws Exception {
            Set<T> models = new HashSet<>();
            HashMap<String, HashMap<String, Object>> value = (HashMap<String, HashMap<String, Object>>) dataSnapshot.getValue();
            for (Map.Entry<String, HashMap<String, Object>> stringHashMapEntry : value.entrySet()) {
                T model = readModel(stringHashMapEntry.getKey(), stringHashMapEntry.getValue(), modelClass);
                models.add(model);
            }
            return models;
        }
    }

    public <T> Task<T> get(final String id, final Class<T> modelClass) {
        ListenableFutureTask<T> futureTask = ListenableFutureTask.create(new DatabaseSingleGetCallable<T>(id, modelClass));
        executor.execute(futureTask);
        final DatabaseGetTask<T> databaseGetTask = new DatabaseGetTask<>();
        Futures.addCallback(futureTask, new FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                databaseGetTask.setResult(result);
            }

            @Override
            public void onFailure(Throwable t) {
                databaseGetTask.setException(t);
            }
        }, mainLooperExecutor);
        return databaseGetTask;
    }

    private final MainLooperExecutor mainLooperExecutor = new MainLooperExecutor();

    private class MainLooperExecutor implements Executor {

        @Override
        public void execute(Runnable runnable) {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    private <T> T newInstanceOf(Class<T> clazz) {
        //noinspection TryWithIdenticalCatches
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
