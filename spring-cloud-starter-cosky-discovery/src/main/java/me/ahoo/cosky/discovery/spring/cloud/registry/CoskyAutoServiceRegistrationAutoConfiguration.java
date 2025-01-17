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

package me.ahoo.cosky.discovery.spring.cloud.registry;

import com.google.common.base.Strings;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import lombok.var;
import me.ahoo.cosky.core.redis.RedisConnectionFactory;
import me.ahoo.cosky.discovery.*;
import me.ahoo.cosky.discovery.redis.RedisServiceRegistry;
import me.ahoo.cosky.discovery.spring.cloud.discovery.ConditionalOnCoskyDiscoveryEnabled;
import me.ahoo.cosky.discovery.spring.cloud.discovery.CoskyDiscoveryAutoConfiguration;
import me.ahoo.cosky.spring.cloud.support.AppSupport;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author ahoo wang
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnCoskyDiscoveryEnabled
@EnableConfigurationProperties(CoskyRegistryProperties.class)
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureBefore({AutoServiceRegistrationAutoConfiguration.class})
@AutoConfigureAfter({CoskyDiscoveryAutoConfiguration.class})
public class CoskyAutoServiceRegistrationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RegistryProperties registryProperties(
            CoskyRegistryProperties coskyRegistryProperties) {
        var registryProperties = new RegistryProperties();
        registryProperties.setInstanceTtl(coskyRegistryProperties.getTtl());
        return registryProperties;
    }

    @Bean
    @Primary
    public RedisServiceRegistry redisServiceRegistry(RegistryProperties registryProperties,
                                                     RedisConnectionFactory redisConnectionFactory) {
        return new RedisServiceRegistry(registryProperties, redisConnectionFactory.getShareReactiveCommands());
    }

    @Bean
    public RenewInstanceService renewInstanceService(CoskyRegistryProperties coskyRegistryProperties, RedisServiceRegistry redisServiceRegistry) {
        return new RenewInstanceService(coskyRegistryProperties.getRenew(), redisServiceRegistry);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(CoskyRegistration.class)
    public CoskyRegistration coskyRegistration(
            ApplicationContext context, CoskyRegistryProperties properties) {
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setMetadata(properties.getMetadata());

        if (Strings.isNullOrEmpty(properties.getServiceId())) {
            String serviceId = AppSupport.getAppName(context.getEnvironment());
            serviceInstance.setServiceId(serviceId);
        } else {
            serviceInstance.setServiceId(properties.getServiceId());
        }

        if (!Strings.isNullOrEmpty(properties.getSchema())) {
            serviceInstance.setSchema(properties.getSchema());
        }

        if (!Strings.isNullOrEmpty(properties.getHost())) {
            serviceInstance.setHost(properties.getHost());
        }

        serviceInstance.setPort(properties.getPort());
        serviceInstance.setWeight(properties.getWeight());
        serviceInstance.setEphemeral(properties.isEphemeral());
        serviceInstance.setInstanceId(InstanceIdGenerator.DEFAULT.generate(serviceInstance));
        return new CoskyRegistration(serviceInstance);
    }

    @Bean
    @Primary
    public CoskyServiceRegistry coskyServiceRegistry(ServiceRegistry serviceRegistry, RenewInstanceService renewInstanceService, CoskyRegistryProperties coskyRegistryProperties) {
        return new CoskyServiceRegistry(serviceRegistry, renewInstanceService, coskyRegistryProperties);
    }

    @Bean
    @Primary
    public CoskyAutoServiceRegistration coskyAutoServiceRegistration(
            CoskyServiceRegistry serviceRegistry,
            CoskyRegistration registration,
            AutoServiceRegistrationProperties autoServiceRegistrationProperties
    ) {
        return new CoskyAutoServiceRegistration(serviceRegistry, registration, autoServiceRegistrationProperties);
    }

    @Bean
    public CoskyAutoServiceRegistrationOfNoneWeb coskyAutoServiceRegistrationOfNoneWeb(
            CoskyServiceRegistry serviceRegistry,
            CoskyRegistration registration
    ) {
        return new CoskyAutoServiceRegistrationOfNoneWeb(serviceRegistry, registration);
    }
}
