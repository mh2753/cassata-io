/*
 * Copyright (c) 2018. cassata.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.cassata.commons.bootstrap;

public enum DatabaseTypes {
    DERBY,
    MYSQL;

    public static DatabaseTypes fromString(String value) {
        for(DatabaseTypes v : values()) {
            if (v.name().equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}
