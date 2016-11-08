package gash.router.server.resources;

import com.google.protobuf.GeneratedMessage;
import gash.router.server.queue.ChannelQueue;
import global.Global;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

public class Failure extends Resource {

    public Failure(ChannelQueue sq){
        super(sq);
    }

    public void handleGlobalCommand(Global.GlobalMessage msg) {

    }

    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to be implement
    }

    public void handleWork(Work.WorkRequest msg) {
        Common.Failure err = msg.getPayload().getErr();
        logger.error("failure from " + msg.getHeader().getNodeId());
    }


}
