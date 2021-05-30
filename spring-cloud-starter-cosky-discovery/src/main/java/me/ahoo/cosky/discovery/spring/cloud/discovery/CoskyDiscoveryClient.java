package me.ahoo.cosky.discovery.spring.cloud.discovery;

import me.ahoo.cosky.core.util.Futures;
import me.ahoo.cosky.discovery.ServiceDiscovery;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ahoo wang
 */
public class CoskyDiscoveryClient implements DiscoveryClient {
    private final ServiceDiscovery serviceDiscovery;
    private final CoskyDiscoveryProperties governDiscoveryProperties;

    public CoskyDiscoveryClient(ServiceDiscovery serviceDiscovery, CoskyDiscoveryProperties governDiscoveryProperties) {
        this.serviceDiscovery = serviceDiscovery;
        this.governDiscoveryProperties = governDiscoveryProperties;
    }

    /**
     * A human-readable description of the implementation, used in HealthIndicator.
     *
     * @return The description.
     */
    @Override
    public String description() {
        return "Govern Service Discovery Client On Redis";
    }

    /**
     * Gets all ServiceInstances associated with a particular serviceId.
     *
     * @param serviceId The serviceId to query.
     * @return A List of ServiceInstance.
     */
    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        return Futures.getUnChecked(serviceDiscovery.getInstances(serviceId), governDiscoveryProperties.getTimeout())
                .stream().map(serviceInstance -> new CoskyServiceInstance(serviceInstance))
                .collect(Collectors.toList());
    }

    /**
     * @return All known service IDs.
     */
    @Override
    public List<String> getServices() {
        return Futures.getUnChecked(serviceDiscovery.getServices(), governDiscoveryProperties.getTimeout())
                .stream().collect(Collectors.toList());
    }

    @Override
    public int getOrder() {
        return governDiscoveryProperties.getOrder();
    }
}