package me.ahoo.govern.core.redis;

import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.govern.core.NamespaceService;
import me.ahoo.govern.core.TestRedisClient;
import me.ahoo.govern.core.listener.RedisMessageListenable;
import org.junit.jupiter.api.*;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ahoo wang
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisNamespaceServiceTest {
    NamespaceService namespaceService;
    private RedisClient redisClient;

    @BeforeAll
    private void init() {
        redisClient = TestRedisClient.createClient();
        namespaceService = new RedisNamespaceService(redisClient.connect().async());
    }

    @Test
    void getNamespaces() {
        var namespaces = namespaceService.getNamespaces().join();
        Assertions.assertNotNull(namespaces);
    }

    @Test
    void setNamespace() {
        var ns = UUID.randomUUID().toString();
        namespaceService.removeNamespace(ns).join();
        var isOk = namespaceService.setNamespace(ns).join();
        Assertions.assertTrue(isOk);
    }

    @Test
    void removeNamespace() {
        var ns = UUID.randomUUID().toString();
        namespaceService.setNamespace(ns).join();
        var isOk = namespaceService.removeNamespace(ns).join();
        Assertions.assertTrue(isOk);
    }

    @AfterAll
    private void destroy() {
        if (Objects.nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }
}