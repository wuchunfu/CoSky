/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosky.spring.cloud.support;

import org.springframework.core.env.Environment;

/**
 * @author ahoo wang
 */
public final class AppSupport {

    public final static String SPRING_APPLICATION_NAME = "spring.application.name";

    private AppSupport() {
    }

    public static String getAppName(Environment environment) {
        return environment.getProperty(SPRING_APPLICATION_NAME);
    }
}
