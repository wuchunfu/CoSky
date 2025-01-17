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

package me.ahoo.cosky.rest.security;

import me.ahoo.cosky.rest.security.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author ahoo wang
 */
@Component
public class SecurityCommand implements CommandLineRunner {
    private final SecurityProperties securityProperties;
    private final UserService userService;

    public SecurityCommand(SecurityProperties securityProperties, UserService userService) {
        this.securityProperties = securityProperties;
        this.userService = userService;
    }

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        userService.initRoot(securityProperties.isEnforceInitSuperUser()).subscribe();
    }
}
