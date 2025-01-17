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

package me.ahoo.cosky.config;

import com.google.common.base.Objects;
import me.ahoo.cosky.core.Namespaced;

/**
 * @author ahoo wang
 */
public class NamespacedConfigId implements Namespaced {
    private final String namespace;
    private final String configId;

    public NamespacedConfigId(String namespace, String configId) {
        this.namespace = namespace;
        this.configId = configId;
    }

    public static NamespacedConfigId of(String namespace, String configId) {
        return new NamespacedConfigId(namespace, configId);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public String getConfigId() {
        return configId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamespacedConfigId)) return false;
        NamespacedConfigId that = (NamespacedConfigId) o;
        return Objects.equal(namespace, that.namespace) && Objects.equal(configId, that.configId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(namespace, configId);
    }
}
