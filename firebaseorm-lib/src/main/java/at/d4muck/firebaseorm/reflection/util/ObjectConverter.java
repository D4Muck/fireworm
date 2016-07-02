package at.d4muck.firebaseorm.reflection.util;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import at.d4muck.firebaseorm.reflection.util.converter.Converter;

/**
 * @author Christoph Muck
 */
@Singleton
public class ObjectConverter {

    private final Set<Converter> converters;

    @Inject
    ObjectConverter(Set<Converter> converters) {
        this.converters = converters;
    }

    public <T> T cast(Object from, Class<T> target) {
        synchronized (converters) {
            for (Converter converter : converters) {
                if (converter.canConvertTo(target)) {
                    return converter.convert(from, target);
                }
            }
        }
        throw new UnsupportedOperationException("Cannot find converter for type: " + target.getCanonicalName());
    }

}
