package me.ahoo.govern.discovery;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.govern.discovery.redis.RedisServiceDiscovery;
import me.ahoo.govern.discovery.redis.RedisServiceRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * @author ahoo wang
 */
@Slf4j
public class RedisServiceDiscoveryTest extends BaseOnRedisClientTest {
    private final static String namespace = "test_svc";
    private RedisServiceDiscovery redisServiceDiscovery;
    private RedisServiceRegistry redisServiceRegistry;

    @BeforeAll
    private void init() {
        var registryProperties = new RegistryProperties();
        redisServiceRegistry = new RedisServiceRegistry(registryProperties, redisConnection.async());
        redisServiceDiscovery = new RedisServiceDiscovery(redisConnection.async());
    }

    private final static int REPEATED_SIZE = 60000;

    @Test
    public void getServices() {
        registerRandomInstanceFinal(namespace, redisServiceRegistry, (instance -> {
            var serviceIds = redisServiceDiscovery.getServices(namespace).join();
            Assertions.assertNotNull(serviceIds);
            Assertions.assertTrue(serviceIds.contains(instance.getServiceId()));
        }));
    }

    @Test
    public void getInstances() {
        registerRandomInstanceFinal(namespace, redisServiceRegistry, (instance -> {
            var instances = redisServiceDiscovery.getInstances(namespace, instance.getServiceId()).join();
            Assertions.assertNotNull(instances);
            Assertions.assertEquals(1, instances.size());

            var expectedInstance = instances.stream().findFirst().get();
            Assertions.assertEquals(instance.getServiceId(), expectedInstance.getServiceId());
            Assertions.assertEquals(instance.getInstanceId(), expectedInstance.getInstanceId());
        }));
    }


    @Test
    public void getInstance() {
        registerRandomInstanceFinal(namespace, redisServiceRegistry, (instance -> {
            var actualInstance = redisServiceDiscovery.getInstance(namespace, instance.getServiceId(), instance.getInstanceId()).join();
            Assertions.assertEquals(instance.getServiceId(), actualInstance.getServiceId());
            Assertions.assertEquals(instance.getInstanceId(), actualInstance.getInstanceId());
        }));
    }


    //    @Test
    public void getServicesRepeatedAsync() {
        var futures = new CompletableFuture[REPEATED_SIZE];
        for (int i = 0; i < REPEATED_SIZE; i++) {
            futures[i] = redisServiceDiscovery.getServices();
        }
        CompletableFuture.allOf(futures).join();
    }

    //    @Test
    public void getInstancesRepeated() {
        for (int i = 0; i < 40000; i++) {
            getInstances();
        }
    }

    //    @Test
    public void getInstancesRepeatedMMultiple() throws InterruptedException {
        int threadCount = 50;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int thNum = 0; thNum < threadCount; thNum++) {
            new Thread(() -> {
                getInstancesRepeated();
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
    }

}