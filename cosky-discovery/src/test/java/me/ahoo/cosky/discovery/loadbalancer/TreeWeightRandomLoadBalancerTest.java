package me.ahoo.cosky.discovery.loadbalancer;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import me.ahoo.cosky.discovery.TestServiceInstance;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author ahoo wang
 */
@Slf4j
class TreeWeightRandomLoadBalancerTest {


    @Test
    public void choose() {
        var serviceId = "ServiceInstanceTree";
        var instance1 = TestServiceInstance.createInstance(serviceId);
        instance1.setWeight(2);
        var instance2 = TestServiceInstance.createInstance(serviceId);
        instance2.setWeight(3);
        var instance3 = TestServiceInstance.createInstance(serviceId);
        instance3.setWeight(5);
        var instances = Arrays.asList(instance1,
                instance2,
                instance3);
        TreeWeightRandomLoadBalancer.TreeChooser treeChooser = new TreeWeightRandomLoadBalancer.TreeChooser(instances);
        var instance = treeChooser.choose();
        assertNotNull(instance);

        int totalTimes = 1000_000_0;
        int instance1Count = 0;
        int instance2Count = 0;
        int instance3Count = 0;
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i = 0; i < totalTimes; i++) {
            var randomInstance = treeChooser.choose();
            if (randomInstance.equals(instance1)) {
                instance1Count++;
            } else if (randomInstance.equals(instance2)) {
                instance2Count++;
            } else {
                instance3Count++;
            }
        }
        log.info("totalTimes:{} | [{}:{},{}:{},{}:{}] taken:[{}]",
                totalTimes,
                instance1Count, instance1Count * 1.0 / totalTimes,
                instance2Count, instance2Count * 1.0 / totalTimes,
                instance3Count, instance3Count * 1.0 / totalTimes,
                stopwatch.elapsed(TimeUnit.MILLISECONDS));

    }

}