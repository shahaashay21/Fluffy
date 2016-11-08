package gash.router.server.resources;

import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelCommandQueue;
import gash.router.server.queue.PerChannelGlobalCommandQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import global.Global;
import pipe.work.Work;
import routing.Pipe;

public class Task extends Resource {

    public Task(ChannelQueue sq){
        this.sq = sq;
    }

    public void handleGlobalCommand(Global.GlobalMessage msg) {

        if(!(sq instanceof PerChannelGlobalCommandQueue)){
            logger.info("Setup queue is not global queue");
            return;
        }

    }

    public void handleCommand(Pipe.CommandRequest msg) {

        if(!(sq instanceof PerChannelCommandQueue)){
            logger.info("Setup queue is not command queue");
            return;
        }
    }

    public void handleWork(Work.WorkRequest msg) {

        if(!(sq instanceof PerChannelWorkQueue)){
            logger.info("Setup queue is not work queue");
            return;
        }
        Work.Task t = msg.getPayload().getTask();
        ((PerChannelWorkQueue)sq).gerServerState().getTasks().addTask(t);
    }


}
