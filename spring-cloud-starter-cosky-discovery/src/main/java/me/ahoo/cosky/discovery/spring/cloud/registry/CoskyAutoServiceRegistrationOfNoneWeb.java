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

package me.ahoo.cosky.discovery.spring.cloud.registry;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.cosky.core.util.Systems;
import me.ahoo.cosky.discovery.InstanceIdGenerator;
import me.ahoo.cosky.discovery.ServiceInstanceContext;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import javax.annotation.PreDestroy;

/**
 * @author ahoo wang
 */
@Slf4j
public class CoskyAutoServiceRegistrationOfNoneWeb implements ApplicationListener<ApplicationStartedEvent>, ApplicationContextAware {

    private final CoskyServiceRegistry serviceRegistry;
    private final CoskyRegistration registration;
    private boolean isWebApp;

    public CoskyAutoServiceRegistrationOfNoneWeb(CoskyServiceRegistry serviceRegistry,
                                                 CoskyRegistration registration) {
        this.serviceRegistry = serviceRegistry;
        this.registration = registration;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (isWebApp) {
            if (log.isInfoEnabled()) {
                log.info("onApplicationEvent - Ignoring registration service of WebServerApplicationContext");
            }
            return;
        }
        var serviceInstance = this.registration.of();
        if (serviceInstance.getPort() == 0) {
            /**
             * use PID as port
             */
            serviceInstance.setSchema("__");
            serviceInstance.setPort((int) Systems.getCurrentProcessId());
            serviceInstance.setInstanceId(InstanceIdGenerator.DEFAULT.generate(serviceInstance));
        }
        ServiceInstanceContext.CURRENT.setServiceInstance(serviceInstance);
        this.serviceRegistry.register(registration);
    }

    @PreDestroy
    public void destroy() {
        if (isWebApp) {
            return;
        }
        this.serviceRegistry.deregister(registration);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        isWebApp = applicationContext instanceof WebServerApplicationContext;
    }
}
