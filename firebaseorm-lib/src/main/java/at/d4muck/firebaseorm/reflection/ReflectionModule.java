package at.d4muck.firebaseorm.reflection;

import javax.inject.Singleton;

import at.d4muck.firebaseorm.reflection.util.converter.Converter;
import at.d4muck.firebaseorm.reflection.util.converter.DateConverter;
import at.d4muck.firebaseorm.reflection.util.converter.EnumConverter;
import at.d4muck.firebaseorm.reflection.util.converter.StringConverter;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;

/**
 * @author Christoph Muck
 */
@Module
public class ReflectionModule {

    @Provides
    @Singleton
    @IntoSet
    static Converter provideStringConverter() {
        return new StringConverter();
    }

    @Provides
    @Singleton
    @IntoSet
    static Converter provideEnumConverter() {
        return new EnumConverter();
    }

    @Provides
    @Singleton
    @IntoSet
    static Converter provideDateConverter() {
        return new DateConverter();
    }

}
