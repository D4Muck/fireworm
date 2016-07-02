package at.d4muck.firebaseorm;

import javax.inject.Singleton;

import at.d4muck.firebaseorm.reflection.ReflectionModule;
import at.d4muck.firebaseorm.repository.Database;
import at.d4muck.firebaseorm.repository.FirebaseModule;
import dagger.Component;

/**
 * Created by cmuck on 02.07.16.
 */
@Singleton
@Component(
        modules = {
                ReflectionModule.class,
                FirebaseModule.class
        }
)
interface AppComponent {
    Database database();
}
