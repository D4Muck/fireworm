package at.d4muck.firebaseorm.reflection.util.converter;

/**
 * @author Christoph Muck
 */
public class StringConverter extends FromStringConverter {

    @Override
    public <T> boolean canConvertTo(Class<T> target) {
        return String.class.equals(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    <T> T convertFromString(String s, Class<T> to) {
        return (T) s;
    }
}
