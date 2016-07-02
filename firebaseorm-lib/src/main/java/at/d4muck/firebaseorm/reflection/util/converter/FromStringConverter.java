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
