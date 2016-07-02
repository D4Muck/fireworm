package at.d4muck.firebaseorm.reflection.util.converter;

/**
 * @author Christoph Muck
 */

public interface Converter {

    <T> boolean canConvertTo(Class<T> target);

    <T> T convert(Object from, Class<T> to);
}
