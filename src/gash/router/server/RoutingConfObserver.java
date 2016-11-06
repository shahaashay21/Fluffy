package gash.router.server;

import gash.router.container.RoutingConf;

/**
 * Created by n on 3/20/16.
 */
public interface RoutingConfObserver {
    public void updateRoutingConf(RoutingConf newConf);
}
