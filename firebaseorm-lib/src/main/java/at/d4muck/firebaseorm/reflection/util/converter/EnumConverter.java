package at.d4muck.firebaseorm.reflection.util.converter;

/**
 * @author Christoph Muck
 */
public class EnumConverter extends FromStringConverter {

    @Override
    public <T> boolean canConvertTo(Class<T> target) {
        return target.isEnum();
    }

    @Override
    @SuppressWarnings("unchecked")
    <T> T convertFromString(String s, Class<T> to) {
        return (T) Enum.valueOf((Class<? extends Enum>) to, s);
    }
}
