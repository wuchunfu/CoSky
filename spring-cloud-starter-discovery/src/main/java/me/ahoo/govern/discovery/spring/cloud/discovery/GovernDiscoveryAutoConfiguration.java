package me.ahoo.govern.discovery.spring.cloud.discovery;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import me.ahoo.govern.core.listener.MessageListenable;
import me.ahoo.govern.discovery.ServiceDiscovery;
import me.ahoo.govern.discovery.loadbalancer.BinaryWeightRandomLoadBalancer;
import me.ahoo.govern.discovery.loadbalancer.LoadBalancer;
import me.ahoo.govern.discovery.redis.ConsistencyRedisServiceDiscovery;
import me.ahoo.govern.discovery.redis.RedisServiceDiscovery;
import me.ahoo.govern.discovery.redis.RedisServiceStatistic;
import me.ahoo.govern.spring.cloud.GovernAutoConfiguration;
import me.ahoo.govern.spring.cloud.support.RedisClientSupport;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author ahoo wang
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@EnableConfigurationProperties({GovernDiscoveryProperties.class})
@AutoConfigureAfter(GovernAutoConfiguration.class)
@AutoConfigureBefore({CommonsClientAutoConfiguration.class})
public class GovernDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisServiceDiscovery redisServiceDiscovery(
            AbstractRedisClient redisClient) {
        RedisClusterAsyncCommands<String, String> redisCommands = RedisClientSupport.getRedisCommands(redisClient);
        return new RedisServiceDiscovery(redisCommands);
    }

    @Bean
    @ConditionalOnMissingBean
    @Primary
    public ConsistencyRedisServiceDiscovery consistencyRedisServiceDiscovery(
            RedisServiceDiscovery redisServiceDiscovery,
            MessageListenable messageListenable) {
        return new ConsistencyRedisServiceDiscovery(redisServiceDiscovery, messageListenable);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisServiceStatistic redisServiceStatistic(
            AbstractRedisClient redisClient,
            MessageListenable messageListenable) {
        RedisClusterAsyncCommands<String, String> redisCommands = RedisClientSupport.getRedisCommands(redisClient);
        return new RedisServiceStatistic(redisCommands, messageListenable);
    }

    @Bean
    @ConditionalOnMissingBean
    public GovernDiscoveryClient governDiscoveryClient(
            ServiceDiscovery serviceDiscovery, GovernDiscoveryProperties governDiscoveryProperties) {
        return new GovernDiscoveryClient(serviceDiscovery, governDiscoveryProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancer loadBalancer(
            ConsistencyRedisServiceDiscovery serviceDiscovery) {
        return new BinaryWeightRandomLoadBalancer(serviceDiscovery);
    }
}