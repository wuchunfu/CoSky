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

package me.ahoo.cosky.core.listener;

import io.lettuce.core.pubsub.api.reactive.PatternMessage;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author ahoo wang
 */
@Slf4j
public class PatternMessageSubscriber extends BaseSubscriber<PatternMessage<String, String>> {
    private final RedisPubSubReactiveCommands<String, String> pubSubReactiveCommands;
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<MessageListener>> listeners;

    public PatternMessageSubscriber(RedisPubSubReactiveCommands<String, String> pubSubReactiveCommands) {
        this.pubSubReactiveCommands = pubSubReactiveCommands;
        this.listeners = new ConcurrentHashMap<>();
        this.pubSubReactiveCommands.observePatterns().subscribe(this);
    }

    public void addListener(String pattern, MessageListener messageListener) {
        listeners.compute(pattern, (key, val) -> {
            if (Objects.isNull(val)) {
                val = new CopyOnWriteArraySet<>();
            }
            if (val.isEmpty()) {
                if (log.isInfoEnabled()) {
                    log.info("addListener - psubscribe - [{}]", key);
                }
                pubSubReactiveCommands.psubscribe(key).subscribe();
            }
            val.add(messageListener);
            return val;
        });
    }

    public void removeListener(String pattern, MessageListener messageListener) {
        listeners.compute(pattern, (key, val) -> {
            if (Objects.isNull(val)) {
                return null;
            }
            if (val.remove(messageListener) && val.isEmpty()) {
                if (log.isInfoEnabled()) {
                    log.info("removeListener - punsubscribe - [{}]", key);
                }
                pubSubReactiveCommands.punsubscribe(key).subscribe();
            }
            return val;
        });
    }

    @Override
    protected void hookOnNext(PatternMessage<String, String> value) {

        if (log.isDebugEnabled()) {
            log.debug("hookOnNext - pattern:[{}] - channel:[{}] - message:[{}].", value.getPattern(), value.getChannel(), value.getMessage());
        }

        CopyOnWriteArraySet<MessageListener> listeners = this.listeners.get(value.getPattern());
        if (Objects.isNull(listeners) || listeners.isEmpty()) {
            if(log.isDebugEnabled()){
                log.debug("hookOnNext - pattern:[{}] - channel:[{}] - message:[{}] - listeners is empty.", value.getPattern(),value.getChannel(), value.getMessage());
            }
            return;
        }

        listeners.forEach(messageListener -> messageListener.onMessage(value.getPattern(), value.getChannel(), value.getMessage()));
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        if (log.isErrorEnabled()) {
            log.error(throwable.getMessage(), throwable);
        }
    }

    @Override
    protected void hookOnCancel() {
        if (log.isInfoEnabled()) {
            log.info("hookOnCancel.");
        }
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        if (log.isInfoEnabled()) {
            log.info("hookOnSubscribe.");
        }
        super.hookOnSubscribe(subscription);
    }

    @Override
    protected void hookOnComplete() {
        if (log.isInfoEnabled()) {
            log.info("hookOnComplete.");
        }
    }
}
