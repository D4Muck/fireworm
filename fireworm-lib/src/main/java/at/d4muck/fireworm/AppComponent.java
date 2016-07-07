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

package at.d4muck.fireworm;

import javax.inject.Singleton;

import at.d4muck.fireworm.reflection.ReflectionModule;
import at.d4muck.fireworm.repository.Database;
import at.d4muck.fireworm.repository.FirebaseModule;
import dagger.Component;

/**
 * Created by cmuck on 02.07.16.
 */
@Singleton
@Component(
        modules = {
                ReflectionModule.class,
                FirebaseModule.class
        }
)
interface AppComponent {
    Database database();
}
