package at.d4muck.firebaseorm.repository;

import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by cmuck on 02.07.16.
 */
@Module
public class FirebaseModule {

    @Provides
    @Singleton
    public static FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }
}
