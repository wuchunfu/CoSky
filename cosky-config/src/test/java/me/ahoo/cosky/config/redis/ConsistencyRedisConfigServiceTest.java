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

package me.ahoo.cosky.config.redis;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.cosky.config.Config;
import me.ahoo.cosky.config.ConfigChangedListener;
import me.ahoo.cosky.config.NamespacedConfigId;
import me.ahoo.cosky.core.listener.DefaultMessageListenable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ahoo wang
 */
@Slf4j
class ConsistencyRedisConfigServiceTest extends BaseOnRedisClientTest {

    private ConsistencyRedisConfigService consistencyRedisConfigService;
    private final String testConfigId = "test_config";
    private final String namespace = "test_cfg_csy";

    @BeforeEach
    private void init() {
        var redisConfigService = new RedisConfigService(redisConnection.reactive());
        consistencyRedisConfigService = new ConsistencyRedisConfigService(redisConfigService, new DefaultMessageListenable(redisClient.connectPubSub().reactive()));
    }

    @Test
    void getConfig() {
        clearTestData(namespace);
        var getConfigData = "getConfigData";
        var setResult = consistencyRedisConfigService.setConfig(namespace, testConfigId, getConfigData).block();
        Assertions.assertTrue(setResult);
        var getResult = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNotNull(getResult);
        Assertions.assertEquals(testConfigId, getResult.getConfigId());
        Assertions.assertEquals(getConfigData, getResult.getData());
        Assertions.assertEquals(1, getResult.getVersion());
        var getResult2 = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertTrue(getResult2 == getResult);
    }

    private final static int SLEEP_FOR_WAIT_MESSAGE = 1;

    @SneakyThrows
    protected void sleepForWaitNotify() {
        TimeUnit.SECONDS.sleep(SLEEP_FOR_WAIT_MESSAGE);
    }

    @Test
    void getConfigChanged() {
        clearTestData(namespace);
        var getResultOp = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNull(getResultOp);
        var getConfigData = "getConfigData";
        var setResult = consistencyRedisConfigService.setConfig(namespace, testConfigId, getConfigData).block();
        Assertions.assertTrue(setResult);
        sleepForWaitNotify();
        Config getResult = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNotNull(getResult);
        var getConfigData2 = "getConfigData-2";
        setResult = consistencyRedisConfigService.setConfig(namespace, testConfigId, getConfigData2).block();
        Assertions.assertTrue(setResult);
        sleepForWaitNotify();
        var getResult2 = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertEquals(getConfigData2, getResult2.getData());
        Assertions.assertNotEquals(getResult, getResult2);
    }

    @Test
    void getConfigChangedRemove() {
        clearTestData(namespace);
        var getResult = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNull(getResult);
        var getConfigData = "getConfigChangedRemoveData";
        var setResult = consistencyRedisConfigService.setConfig(namespace, testConfigId, getConfigData).block();
        Assertions.assertTrue(setResult);
        sleepForWaitNotify();
        getResult = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNotNull(getResult);
        var removeResult = consistencyRedisConfigService.removeConfig(namespace, testConfigId).block();
        Assertions.assertTrue(removeResult);
        sleepForWaitNotify();
        var getResult2 = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNull(getResult2);
    }

    @Test
    void getConfigChangedRollback() {
        clearTestData(namespace);
        var version1Data = "version-1";
        var setResult = consistencyRedisConfigService.setConfig(namespace, testConfigId, version1Data).block();
        Assertions.assertTrue(setResult);
        var version1Config = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNotNull(version1Config);
        Assertions.assertEquals(version1Data, version1Config.getData());

        var version2Data = "version-2";
        setResult = consistencyRedisConfigService.setConfig(namespace, testConfigId, version2Data).block();
        Assertions.assertTrue(setResult);
        sleepForWaitNotify();
        var version2Config = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();
        Assertions.assertNotNull(version2Config);
        Assertions.assertEquals(version2Data, version2Config.getData());

        var rollbackResult = consistencyRedisConfigService.rollback(namespace, testConfigId, version1Config.getVersion()).block();
        Assertions.assertTrue(rollbackResult);
        sleepForWaitNotify();
        var afterRollbackConfig = consistencyRedisConfigService.getConfig(namespace, testConfigId).block();

        Assertions.assertEquals(version1Config.getData(), afterRollbackConfig.getData());
    }

    @SneakyThrows
    @Test
    void addListener() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<NamespacedConfigId> changedConfigId = new AtomicReference<>();
        var testConfig = NamespacedConfigId.of(namespace, testConfigId);
        var changedListener = new ConfigChangedListener() {
            @Override
            public void onChange(NamespacedConfigId namespacedConfigId, String op) {
                log.warn("addListener@Test - configId:[{}] - message:[{}]", namespacedConfigId, op);
                changedConfigId.set(namespacedConfigId);
                countDownLatch.countDown();
            }
        };
        consistencyRedisConfigService.addListener(testConfig, changedListener);
        getConfigChanged();
        countDownLatch.await();
        Assertions.assertEquals(testConfig, changedConfigId.get());
    }

    @Test
    void removeListener() {
        var testConfig = NamespacedConfigId.of(namespace, testConfigId);
        var changedListener = new ConfigChangedListener() {
            @Override
            public void onChange(NamespacedConfigId namespacedConfigId, String op) {
                Assertions.fail();
            }
        };
        consistencyRedisConfigService.addListener(testConfig, changedListener);

        consistencyRedisConfigService.removeListener(testConfig, changedListener);

        getConfigChanged();
    }
}
