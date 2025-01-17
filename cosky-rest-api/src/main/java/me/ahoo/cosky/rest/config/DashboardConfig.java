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

package me.ahoo.cosky.rest.config;

import me.ahoo.cosky.rest.support.RequestPathPrefix;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * for Dashboard-UI
 *
 * @author ahoo wang
 */
@Controller
public class DashboardConfig {
    public static final String INDEX_PAGE = RequestPathPrefix.DASHBOARD + "index.html";
    public static final URI INDEX_PAGE_URI = URI.create(INDEX_PAGE);

    @GetMapping(
            {
                    "/",
                    RequestPathPrefix.DASHBOARD,
                    RequestPathPrefix.DASHBOARD + "home",
                    RequestPathPrefix.DASHBOARD + "topology",
                    RequestPathPrefix.DASHBOARD + "config",
                    RequestPathPrefix.DASHBOARD + "service",
                    RequestPathPrefix.DASHBOARD + "namespace",
                    RequestPathPrefix.DASHBOARD + "user",
                    RequestPathPrefix.DASHBOARD + "role",
                    RequestPathPrefix.DASHBOARD + "audit-log",
                    RequestPathPrefix.DASHBOARD + "login"
            }
    )
    public Mono<Void> home(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        response.getHeaders().setLocation(INDEX_PAGE_URI);
        return response.setComplete();
    }
}
