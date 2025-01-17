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

package me.ahoo.cosky.spring.cloud;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosky.core.CoSky;
import me.ahoo.cosky.core.NamespacedProperties;
import me.ahoo.cosky.core.redis.RedisConfig;
import me.ahoo.cosky.core.util.RedisKeys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author ahoo wang
 */
@ConfigurationProperties(CoskyProperties.PREFIX)
@Slf4j
public class CoskyProperties extends NamespacedProperties {
    public static final String PREFIX = "spring.cloud." + CoSky.COSKY;

    private boolean enabled = true;

    @Override
    public void setNamespace(String namespace) {
        if (RedisConfig.RedisMode.CLUSTER.equals(getRedis().getMode())
                && !RedisKeys.hasWrap(namespace)
        ) {
            String clusterNamespace = RedisKeys.ofKey(true, namespace);
            if (log.isWarnEnabled()) {
                log.warn("When Redis is in cluster mode, namespace:[{}-->{}] must be wrapped by {}(hashtag).", namespace,clusterNamespace, "{}");
            }
            namespace = clusterNamespace;
        }
        super.setNamespace(namespace);
    }

    @NestedConfigurationProperty
    private RedisConfig redis = new RedisConfig();

    public RedisConfig getRedis() {
        return redis;
    }

    public void setRedis(RedisConfig redis) {
        this.redis = redis;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


}
