package me.ahoo.govern.disvoery.spring.cloud.registry;

import me.ahoo.govern.discovery.RenewProperties;
import me.ahoo.govern.disvoery.spring.cloud.discovery.GovernDiscoveryProperties;
import me.ahoo.govern.disvoery.spring.cloud.support.StatusConstants;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ahoo wang
 */
@ConfigurationProperties(GovernRegistryProperties.PREFIX)
public class GovernRegistryProperties {
    public static final String PREFIX = GovernDiscoveryProperties.PREFIX + ".registry";
    private final InetUtils.HostInfo hostInfo;
    private String serviceId;
    private String schema = "http";
    private String ip;
    private int port;

    private int weight = 1;
    private boolean ephemeral = true;
    private String initialStatus = StatusConstants.STATUS_UP;
    private Map<String, String> metadata = new HashMap<>();

    private int ttl = 60;

    private Boolean secure;

    private RenewProperties renew = new RenewProperties();

    public GovernRegistryProperties(InetUtils inetUtils) {
        this.hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
        this.ip = this.hostInfo.getIpAddress();
        metadata.put(StatusConstants.INSTANCE_STATUS_KEY, initialStatus);
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public int getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(String initialStatus) {
        this.initialStatus = initialStatus;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
        if (Strings.isBlank(schema)) {
            schema = secure ? "http" : "https";
        }
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public RenewProperties getRenew() {
        return renew;
    }

    public void setRenew(RenewProperties renew) {
        this.renew = renew;
    }
}