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

import me.ahoo.cosky.discovery.ServiceInstance;
import me.ahoo.cosky.discovery.redis.ConsistencyRedisServiceDiscovery;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ahoo wang
 */
public class RandomLoadBalancer extends AbstractLoadBalancer<RandomLoadBalancer.RandomChooser> {
    public RandomLoadBalancer(ConsistencyRedisServiceDiscovery serviceDiscovery) {
        super(serviceDiscovery);
    }

    @Override
    protected RandomChooser createChooser(List<ServiceInstance> serviceInstances) {
        return new RandomChooser(serviceInstances);
    }


    public static class RandomChooser implements LoadBalancer.Chooser {
        private final List<ServiceInstance> serviceInstances;

        public RandomChooser(List<ServiceInstance> serviceInstances) {
            this.serviceInstances = serviceInstances;
        }

        @Override
        public ServiceInstance choose() {
            if (serviceInstances.isEmpty()) {
                return null;
            }
            if (serviceInstances.size() == ONE) {
                return serviceInstances.get(ZERO);
            }
            int randomIdx = ThreadLocalRandom.current().nextInt(serviceInstances.size());
            return serviceInstances.get(randomIdx);
        }
    }
}
