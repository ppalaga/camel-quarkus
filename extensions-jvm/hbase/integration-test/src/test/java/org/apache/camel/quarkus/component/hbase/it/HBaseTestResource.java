package org.apache.camel.quarkus.component.hbase.it;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.jboss.logging.Logger;

public class HBaseTestResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOG = Logger.getLogger(HBaseTestResource.class);
    // must be the same as in the config of camel component
    private static final Integer CLIENT_PORT = 21818;

    private HBaseTestingUtility hbaseUtil;

    @Override
    public Map<String, String> start() {
        try {
            Configuration conf = HBaseConfiguration.create();
            conf.set("test.hbase.zookeeper.property.clientPort", CLIENT_PORT.toString());
            hbaseUtil = new HBaseTestingUtility(conf);
            hbaseUtil.startMiniCluster(1);
        } catch (Exception e) {
            throw new RuntimeException("Could not start HBase cluster.", e);
        }
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        try {
            hbaseUtil.shutdownMiniCluster();
        } catch (Exception e) {
            LOG.warn("Error shutting down the HBase container", e);
        }
    }
}
