package gash.router.server.tasks;

import gash.router.container.RoutingConf;
import global.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterClusterManager {
    protected static Logger logger = LoggerFactory.getLogger("inter-cluster-manager");

    protected static RoutingConf conf;

    InterClusterManager(RoutingConf conf){
        this.conf= conf;
    }

    public static void sendToAnotherCluster(Global.GlobalMessage msg){
        for(RoutingConf.RoutingEntry re : conf.getAdapterRouting()){

        }
    }


}
