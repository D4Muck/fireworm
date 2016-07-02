package at.d4muck.firebaseorm.reflection.util.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Christoph Muck
 */
public class DateConverter extends FromStringConverter {
    @Override
    public <T> boolean canConvertTo(Class<T> target) {
        return Date.class.equals(target);
    }

    @Override
    <T> T convertFromString(String s, Class<T> to) {
        if (s.isEmpty()) return null;
        DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        try {
            return (T) format.parse(s);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
