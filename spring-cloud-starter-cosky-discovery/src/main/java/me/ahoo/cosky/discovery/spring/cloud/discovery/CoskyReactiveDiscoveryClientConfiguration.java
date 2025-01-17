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

package me.ahoo.cosky.discovery.spring.cloud.discovery;

import me.ahoo.cosky.discovery.ServiceDiscovery;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.*;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ahoo wang
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnCoskyDiscoveryEnabled
@ConditionalOnDiscoveryEnabled
@ConditionalOnReactiveDiscoveryEnabled
@AutoConfigureBefore({ReactiveCommonsClientAutoConfiguration.class})
@AutoConfigureAfter({CoskyDiscoveryAutoConfiguration.class, ReactiveCompositeDiscoveryClientAutoConfiguration.class})
public class CoskyReactiveDiscoveryClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CoskyReactiveDiscoveryClient coskyReactiveDiscoveryClient(ServiceDiscovery serviceDiscovery) {
        return new CoskyReactiveDiscoveryClient(serviceDiscovery);
    }
}
