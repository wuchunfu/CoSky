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

/**
 * @author ahoo wang
 */
public class ConfigHistory extends Config {
    /**
     * set
     * remove
     * rollback
     */
    private String op;
    private Long opTime;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public Long getOpTime() {
        return opTime;
    }

    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigHistory)) return false;
        if (!super.equals(o)) return false;
        ConfigHistory that = (ConfigHistory) o;
        return Objects.equal(op, that.op) && Objects.equal(opTime, that.opTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), op, opTime);
    }
}
