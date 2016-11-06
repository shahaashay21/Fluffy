package gash.router.server.resources;

import com.google.protobuf.GeneratedMessage;
import gash.router.server.queue.ChannelQueue;
import global.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by a on 4/2/16.
 */
public class Resource {

    Logger logger = LoggerFactory.getLogger("Resource Handling");

    ChannelQueue sq;

    Resource(){

    }

    Resource(ChannelQueue sq){
        this.sq = sq;
    }

    void handleGlobalCommand(Global.GlobalCommandMessage msg){

    }

    void handleCommand(Pipe.CommandRequest msg){

    }

    void handleWork(Work.WorkRequest msg){

    }

    public void handle(GeneratedMessage msg) {

        if(msg instanceof Global.GlobalCommandMessage){
            handleGlobalCommand((Global.GlobalCommandMessage) msg);
        }else if(msg instanceof Pipe.CommandRequest){
            handleCommand((Pipe.CommandRequest) msg);
        }else if(msg instanceof Work.WorkRequest){
            handleWork((Work.WorkRequest) msg);
        }

    }
}
