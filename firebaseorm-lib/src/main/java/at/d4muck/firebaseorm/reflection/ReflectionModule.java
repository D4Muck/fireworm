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
