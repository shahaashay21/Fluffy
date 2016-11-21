package gash.router.server.resources;

import com.google.protobuf.GeneratedMessage;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import global.Global;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

import java.util.Iterator;

public class Response extends Resource {

    Common.Response response;

    public Response(ChannelQueue sq){
        super(sq);
    }

    public void handleGlobalCommand(Global.GlobalMessage msg) {

        response = msg.getResponse();

        switch (response.getRequestType()){
            case READ:
                break;
            case WRITE:
                break;
            case UPDATE:
            case DELETE:
                break;
        }

    }

    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to be implement
    }

    public void handleWork(Work.WorkRequest msg) {
        response = msg.getPayload().getResponse();
        logger.debug("Response on work channel from " + msg.getHeader().getNodeId());

        switch (response.getRequestType()) {
            case READ:
                PrintUtil.printWork(msg);
                forwardResponseOntoIncomingChannel(msg,false);
                break;
            case WRITE:
                //todo
                PrintUtil.printWork(msg);
                forwardResponseOntoIncomingChannel(msg,false);
                break;
            case UPDATE:
            case DELETE:
                break;
        }
    }

    private void forwardResponseOntoIncomingChannel(GeneratedMessage msg, boolean glabalCommandMessage){

        Common.Header.Builder hb = Common.Header.newBuilder();

        if(glabalCommandMessage){

        }
        else{
            Work.WorkRequest clientMessage = (Work.WorkRequest) msg;

            if(!clientMessage.getHeader().getSourceHost().contains("_")){
                logger.error("_ source cluster error");

            }
            else {

                Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder(); // message to be returned

                hb.setNodeId(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId());
                hb.setTime(((Work.WorkRequest) msg).getHeader().getTime());
                hb.setDestination(Integer.parseInt(clientMessage.getHeader().getSourceHost().substring(0, clientMessage.getHeader().getSourceHost().indexOf('_'))));// wont be available in case of request from client. but can be determined based on log replication feature
                hb.setSourceHost(clientMessage.getHeader().getSourceHost().substring(clientMessage.getHeader().getSourceHost().indexOf('_') + 1));
                hb.setDestinationHost(clientMessage.getHeader().getDestinationHost()); // would be used to return message back to client
                hb.setMaxHops(((Work.WorkRequest) msg).getHeader().getMaxHops() - 1);

                wb.setHeader(hb);
                wb.setSecret(1234567809);
                wb.setPayload(Work.Payload.newBuilder().setResponse(((Work.WorkRequest) msg).getPayload().getResponse())); // set the reponse to the client

                Iterator<EdgeInfo> inBoundEdgeListIt = MessageServer.getEmon().getInboundEdgeInfoList().iterator();
                while (inBoundEdgeListIt.hasNext()) {
                    EdgeInfo ei = inBoundEdgeListIt.next();
                    if (ei.getRef() == hb.getDestination()) {
                        ei.getQueue().enqueueResponse(wb.build(), null);
                    } else {
                        // connection from where request came is down. // need to handle yet
                    }
                }
            }

        }
    }
}
