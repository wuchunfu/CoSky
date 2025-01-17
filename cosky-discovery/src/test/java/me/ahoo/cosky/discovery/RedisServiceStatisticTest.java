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

package me.ahoo.cosky.discovery;

import lombok.var;
import me.ahoo.cosky.core.listener.DefaultMessageListenable;
import me.ahoo.cosky.discovery.redis.RedisServiceRegistry;
import me.ahoo.cosky.discovery.redis.RedisServiceStatistic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author ahoo wang
 */
public class RedisServiceStatisticTest extends BaseOnRedisClientTest {
    private final static String namespace = "test_svc_stat";
    private RedisServiceStatistic redisServiceStatistic;

    private RedisServiceRegistry redisServiceRegistry;

    @BeforeAll
    private void init() {
        var registryProperties = new RegistryProperties();
        redisServiceRegistry = new RedisServiceRegistry(registryProperties, redisConnection.reactive());
        redisServiceStatistic = new RedisServiceStatistic(redisConnection.reactive(), new DefaultMessageListenable(redisClient.connectPubSub().reactive()));
    }

    @Test
    void statService() {
        var getServiceStatInstance = createRandomInstance();

        redisServiceRegistry.register(namespace, getServiceStatInstance).block();
        redisServiceStatistic.statService(namespace).block();

        var stats = redisServiceStatistic.getServiceStats(namespace).block();
        Assertions.assertTrue(stats.size() >= 1);
        var statOptional = stats.stream().filter(serviceStat -> serviceStat.getServiceId().equals(getServiceStatInstance.getServiceId())).findFirst();
        Assertions.assertTrue(statOptional.isPresent());
        var testStat = statOptional.get();
        Assertions.assertEquals(1, testStat.getInstanceCount());
    }
}
