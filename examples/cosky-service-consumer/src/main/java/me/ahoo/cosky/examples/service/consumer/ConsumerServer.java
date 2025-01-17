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

package me.ahoo.cosky.examples.service.consumer;

import lombok.extern.slf4j.Slf4j;
import me.ahoo.cosky.examples.service.provider.client.HelloClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author ahoo wang
 */
@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = {"me.ahoo.cosky.examples.service.provider.client"})
public class ConsumerServer implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerServer.class, args);
    }

    @Autowired
    private HelloClient helloClient;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        String rpcResponse = helloClient.hi("consumer");
        log.warn(rpcResponse);
    }
}
