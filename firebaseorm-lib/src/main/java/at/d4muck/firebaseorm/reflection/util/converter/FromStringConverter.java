package at.d4muck.firebaseorm.reflection.util.converter;

/**
 * @author Christoph Muck
 */
abstract class FromStringConverter implements Converter {

    @Override
    public <T> T convert(Object from, Class<T> to) {
        if (from instanceof String) {
            return convertFromString((String) from, to);
        }
        throw new IllegalArgumentException("From must be a string instance, is: " + from.getClass().getCanonicalName());
    }

    abstract <T> T convertFromString(String s, Class<T> to);
}
