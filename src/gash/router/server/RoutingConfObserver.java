package gash.router.server;

import gash.router.container.RoutingConf;


public interface RoutingConfObserver {
    public void updateRoutingConf(RoutingConf newConf);
}
