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
