package gash.router.server.listener;

import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;


public class EdgeDisconnectionListener implements ChannelFutureListener {

    EdgeMonitor emon;
    EdgeInfo ei;

    public EdgeDisconnectionListener(EdgeMonitor emon, EdgeInfo ei) {

        this.emon = emon;
        this.ei = ei;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        // we lost the connection or have shutdown.
        System.out.println("--> edge lost. closing channel");
        System.out.flush();
        emon.onRemove(ei);

    }
}
