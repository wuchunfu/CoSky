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

package me.ahoo.cosky.discovery.loadbalancer;

import lombok.var;
import me.ahoo.cosky.core.listener.DefaultMessageListenable;
import me.ahoo.cosky.discovery.BaseOnRedisClientTest;
import me.ahoo.cosky.discovery.RegistryProperties;
import me.ahoo.cosky.discovery.redis.ConsistencyRedisServiceDiscovery;
import me.ahoo.cosky.discovery.redis.RedisServiceDiscovery;
import me.ahoo.cosky.discovery.redis.RedisServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author ahoo wang
 */
class RandomLoadBalancerTest extends BaseOnRedisClientTest {
    private final static String namespace = "test_lb";
    private RedisServiceDiscovery redisServiceDiscovery;
    private RedisServiceRegistry redisServiceRegistry;
    private RandomLoadBalancer randomLoadBalancer;

    @BeforeAll
    private void init() {
        var registryProperties = new RegistryProperties();
        redisServiceRegistry = new RedisServiceRegistry(registryProperties, redisConnection.reactive());
        redisServiceDiscovery = new RedisServiceDiscovery(redisConnection.reactive());
        var consistencyRedisServiceDiscovery = new ConsistencyRedisServiceDiscovery(redisServiceDiscovery, new DefaultMessageListenable(redisClient.connectPubSub().reactive()), redisConnection.reactive());
        randomLoadBalancer = new RandomLoadBalancer(consistencyRedisServiceDiscovery);
    }

    @Test
    void chooseNone() {
        var instance = randomLoadBalancer.choose(namespace, UUID.randomUUID().toString()).block();
        Assertions.assertNull(instance);
    }

    @Test
    void chooseOne() {
        registerRandomInstanceFinal(namespace, redisServiceRegistry, instance -> {
            var expectedInstance = randomLoadBalancer.choose(namespace, instance.getServiceId()).block();
            Assertions.assertEquals(instance.getServiceId(), expectedInstance.getServiceId());
            Assertions.assertEquals(instance.getInstanceId(), expectedInstance.getInstanceId());
        });
    }

    @Test
    void chooseMultiple() {
        var serviceId = UUID.randomUUID().toString();
        var instance1 = createInstance(serviceId);
        var instance2 = createInstance(serviceId);
        var instance3 = createInstance(serviceId);
        redisServiceRegistry.register(namespace, instance1).block();
        redisServiceRegistry.register(namespace, instance2).block();
        redisServiceRegistry.register(namespace, instance3).block();
        var expectedInstance = randomLoadBalancer.choose(namespace, serviceId).block();
        Assertions.assertNotNull(expectedInstance);
        boolean succeeded = expectedInstance.getInstanceId().equals(instance1.getInstanceId())
                || expectedInstance.getInstanceId().equals(instance2.getInstanceId())
                || expectedInstance.getInstanceId().equals(instance3.getInstanceId());
        Assertions.assertTrue(succeeded);
        redisServiceRegistry.deregister(namespace, instance1);
        redisServiceRegistry.deregister(namespace, instance2);
        redisServiceRegistry.deregister(namespace, instance3);
    }
}
