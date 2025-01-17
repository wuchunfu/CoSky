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

import com.google.common.base.Strings;
import lombok.var;

/**
 * @author ahoo wang
 */
public interface InstanceIdGenerator {

    Default DEFAULT = new Default();

    String generate(Instance instance);

    class Default implements InstanceIdGenerator {
        /**
         * {@link  ServiceInstance#getServiceId()}@{@link ServiceInstance#getSchema()}#{@link ServiceInstance#getHost()}#{@link ServiceInstance#getPort()}}
         * order_service@http#127.0.0.1#8088
         */
        public final static String ID_FORMAT = "%s@%s#%s#%s";

        public String generate(Instance instance) {
            return Strings.lenientFormat(ID_FORMAT,
                    instance.getServiceId(),
                    instance.getSchema(),
                    instance.getHost(),
                    instance.getPort()
            );
        }

        public Instance of(String instanceId) {
            var instance = new Instance();
            instance.setInstanceId(instanceId);
            var serviceSpits = instanceId.split("@");
            if (serviceSpits.length != 2) {
                throw new IllegalArgumentException(Strings.lenientFormat("instanceId:[%s] format error.", instanceId));
            }
            instance.setServiceId(serviceSpits[0]);
            var instanceSpits = serviceSpits[1].split("#");
            if (instanceSpits.length != 3) {
                throw new IllegalArgumentException(Strings.lenientFormat("instanceId:[%s] format error.", instanceId));
            }
            instance.setSchema(instanceSpits[0]);
            instance.setHost(instanceSpits[1]);
            instance.setPort(Integer.parseInt(instanceSpits[2]));
            return instance;
        }


    }

}
