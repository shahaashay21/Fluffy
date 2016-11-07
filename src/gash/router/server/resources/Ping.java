package gash.router.server.resources;

import com.google.protobuf.GeneratedMessage;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelCommandQueue;
import gash.router.server.queue.PerChannelGlobalCommandQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import global.Global;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by a on 4/2/16.
 */
public class Ping extends Resource {

    public Ping(ChannelQueue sq){
        super(sq);
    }

    public void handleGlobalCommand(Global.GlobalMessage msg) {

        if(!(sq instanceof PerChannelGlobalCommandQueue)){
            logger.info("Setup queue is not global queue");
            return;
        }
        
        if(msg.getGlobalHeader().getDestinationId() == ((PerChannelGlobalCommandQueue)sq).getRoutingConf().getNodeId()){
            logger.info("ping from " + msg.getGlobalHeader().getClusterId());
        }
        else{ //message doesn't belong to current node. Forward on other edges
            forwardRequestOnWorkChannel(msg,true);
        }



    }

    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to implement over here
        //handle message by self
        if(!(sq instanceof PerChannelCommandQueue)){
            logger.info("Setup queue is not command queue");
            return;
        }

        boolean msgDropFlag;
        if(msg.getHeader().getDestination() == ((PerChannelCommandQueue)sq).getRoutingConf().getNodeId()){
            logger.info("ping from " + msg.getHeader().getNodeId());
        }
        else{ //message doesn't belong to current node. Forward on other edges
            msgDropFlag = true;
            if(MessageServer.getEmon() != null){// forward if Comm-worker port is active
                for(EdgeInfo ei :MessageServer.getEmon().getOutboundEdgeInfoList()){
                    if(ei.isActive() && ei.getChannel() != null){// check if channel of outboundWork edge is active
                        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

                        Common.Header.Builder hb = Common.Header.newBuilder();
                        hb.setNodeId(((PerChannelCommandQueue)sq).getRoutingConf().getNodeId());
                        hb.setTime(msg.getHeader().getTime());
                        hb.setDestination(msg.getHeader().getDestination());
                        hb.setSourceHost(((PerChannelCommandQueue)sq).getRoutingConf().getNodeId()+"_"+msg.getHeader().getSourceHost());
                        hb.setDestinationHost(msg.getHeader().getDestinationHost());
                        hb.setMaxHops(5);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);
                        wb.setPayload(Work.Payload.newBuilder().setPing(true));

                        Work.WorkRequest work = wb.build();

                        PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
                        edgeQueue.enqueueResponse(work,ei.getChannel());
                        msgDropFlag = false;
                        logger.info("Workmessage queued");
                    }
                }
                if(msgDropFlag)
                    logger.info("Message dropped <node,ping,destination>: <" + msg.getHeader().getNodeId()+"," + msg.getPayload().getPing()+"," + msg.getHeader().getDestination()+">");
            }
            else{// drop the message or queue it for limited time to send to connected node
                //todo
                logger.info("No outbound edges to forward. To be handled");
            }

        }
        

    }

    public void handleWork(Work.WorkRequest msg) {
        //handle message by self

        if(!(sq instanceof PerChannelWorkQueue)){
            logger.info("Setup queue is not work queue");
            return;
        }

        logger.debug("ping from <node,host> : <" + msg.getHeader().getNodeId() + ", " + msg.getHeader().getSourceHost()+">");
        PrintUtil.printWork(msg);
        
        
        if(msg.getHeader().getDestination() == ((PerChannelWorkQueue)sq).gerServerState().getConf().getNodeId()){
            logger.debug("Ping for me: " + " from "+ msg.getHeader().getSourceHost());
        }
        else { //message doesn't belong to current node. Forward on other edges
            forwardRequestOnWorkChannel(msg,true);
        }

    }

    private void forwardRequestOnWorkChannel(GeneratedMessage msg, boolean globalCommandMessage){


        boolean msgDropFlag = true;
        if (MessageServer.getEmon() != null) {// forward if Comm-worker port is active
            for (EdgeInfo ei : MessageServer.getEmon().getOutboundEdgeInfoList()) {
                if (ei.isActive() && ei.getChannel() != null) {// check if channel of outboundWork edge is active
                    PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
                    Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder(); // message to be forwarded
                    Common.Header.Builder hb = Common.Header.newBuilder();

                    if(globalCommandMessage) {
                        Global.GlobalMessage clientMessage = (Global.GlobalMessage) msg;

                        hb.setNodeId(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId());
                        hb.setTime(clientMessage.getGlobalHeader().getTime());
                        hb.setDestination(clientMessage.getGlobalHeader().getDestinationId());// wont be available in case of request from client. but can be determined based on log replication feature
                        hb.setSourceHost(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId() + "_" + clientMessage.getGlobalHeader().getClusterId());
                        hb.setDestinationHost(Integer.toString(clientMessage.getGlobalHeader().getClusterId())); // would be used to return message back to client
                        hb.setMaxHops(1);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);
                        wb.setPayload(Work.Payload.newBuilder().setPing(true)); // set the ping from client

                    }
                    else{ // query in work message
                        Work.WorkRequest clientMessage = (Work.WorkRequest) msg;

                        hb.setNodeId(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId());
                        hb.setTime(clientMessage.getHeader().getTime());
                        hb.setDestination(clientMessage.getHeader().getDestination());// wont be available in case of request from client. but can be determined based on log replication feature
                        hb.setSourceHost(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId() + "_" + clientMessage.getHeader().getSourceHost());
                        hb.setDestinationHost(clientMessage.getHeader().getDestinationHost()); // would be used to return message back to client
                        hb.setMaxHops(((Work.WorkRequest) msg).getHeader().getMaxHops() - 1);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);
                        wb.setPayload(Work.Payload.newBuilder().setPing(true)); // set the ping from client

                    }
                    if(hb.getMaxHops() > 0) {
                        Work.WorkRequest work = wb.build();
                        edgeQueue.enqueueResponse(work, ei.getChannel());
                        msgDropFlag = false;
                        logger.info("Workmessage pertaining to client ping queued");
                    }
                    if (msgDropFlag && globalCommandMessage)
                        logger.info("Message dropped <node,ping,source>: <" + ((Global.GlobalMessage) msg).getGlobalHeader().getClusterId()
                                + "," + ((Global.GlobalMessage) msg).getPing() + "," + ((Global.GlobalMessage) msg).getGlobalHeader().getClusterId() + ">");
                    else if(msgDropFlag && !globalCommandMessage)
                        logger.info("Message dropped <node,ping,source>: <" + ((Work.WorkRequest) msg).getHeader().getNodeId()
                                + "," + ((Work.WorkRequest) msg).getPayload().getPing() + "," + ((Work.WorkRequest) msg).getHeader().getSourceHost() + ">");

                }

            }
        } else {// drop the message or queue it for limited time to send to connected node
            //todo
            logger.info("No outbound edges to forward. To be handled");
        }

    }

}
