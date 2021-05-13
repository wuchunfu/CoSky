package me.ahoo.govern.discovery;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author ahoo wang
 */
public interface ServiceStatistic {

    CompletableFuture<Void> statService(String namespace);

    CompletableFuture<Void> statService(String namespace, @Nullable String serviceId);

    CompletableFuture<List<ServiceStat>> getServiceStats(String namespace);
}