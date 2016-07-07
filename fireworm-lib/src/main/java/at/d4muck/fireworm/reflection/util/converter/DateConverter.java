/*
 * Copyright 2016 Christoph Muck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.d4muck.fireworm.reflection.util.converter;

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
