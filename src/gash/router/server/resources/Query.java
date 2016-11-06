package gash.router.server.resources;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import database.dao.MongoDAO;
import database.model.DataModel;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelGlobalCommandQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import global.Global;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;
import storage.Storage;

import java.util.ArrayList;

/**
 * Created by a on 4/3/16.
 */
public class Query extends Resource {

    Storage.Query query;

    public Query(ChannelQueue sq){
        super(sq);
    }


    public void handleGlobalCommand(Global.GlobalCommandMessage msg) {

        query = msg.getQuery();
        //If this have to handle on the same node
        //TODO: change the logic so that it has to be dependent on configuration and intra cluster node space dependent.
        //if (msg.getHeader().getDestination() == ((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId()) {
        //Commenting above line as in request from client destination wouldn't be available
            switch (query.getAction()) {
                case GET:
                    PrintUtil.printGlobalCommand(msg);
                    ArrayList<DataModel> arrRespData = checkIfQueryIsLocalAndGetResponse(query);
                    if(arrRespData.size() > 0){
                        //generate a response message
                        for(DataModel dataModel : arrRespData){
                            Storage.Response response = getResponseMessageForGet(dataModel);
                            generateResponseOntoIncomingChannel(msg,response,true);
                        }
                    }
                    else{
                        forwardRequestOnWorkChannel(msg,true);
                    }
                    break;
                case STORE:
                    PrintUtil.printGlobalCommand(msg);
                    if(MongoDAO.isSufficientSpace("test")){
                        // needs change
                        int result = MongoDAO.saveData("test",new DataModel(query.getKey(),query.getSequenceNo(),query.getData().toByteArray()));
                        Storage.Response response = getResponseMessageForStore(result);
                        logger.debug("Result of save data in mongo :"+ result);
                        generateResponseOntoIncomingChannel(msg,response,true);
                    }
                    else{
                        //forward the message for storage
                        forwardRequestOnWorkChannel(msg,true);
                    }
                    break;
                case UPDATE:
                case DELETE:
                    break;
            }

        //}
    }

    public void handleCommand(Pipe.CommandRequest msg) {
        //Not to be implement
    }

    public void handleWork(Work.WorkRequest msg) {
        Storage.Query query = msg.getPayload().getQuery();
        logger.debug("Query on work channel from " + msg.getHeader().getNodeId());
        switch (query.getAction()) {
            case GET:
                PrintUtil.printWork(msg);
                ArrayList<DataModel> arrRespData = checkIfQueryIsLocalAndGetResponse(query);
                if(arrRespData.size() > 0){
                    //generate a response message
                    for(DataModel dataModel : arrRespData){
                        Storage.Response response = getResponseMessageForGet(dataModel);
                        generateResponseOntoIncomingChannel(msg,response,false);
                    }
                }
                else{
                    forwardRequestOnWorkChannel(msg,false);
                }
                break;
            case STORE:
                PrintUtil.printWork(msg);
                if(MongoDAO.isSufficientSpace("test")){
                    // needs change
                    int result = MongoDAO.saveData("test",new DataModel(query.getKey(),query.getSequenceNo(),query.getData().toByteArray()));
                    Storage.Response response = getResponseMessageForStore(result);
                    logger.debug("Result of save data in mongo :"+ result);
                    generateResponseOntoIncomingChannel(msg,response,false);
                }
                else{
                    //forward the message for storage
                    forwardRequestOnWorkChannel(msg,false);
                }
                break;
            case UPDATE:
            case DELETE:
                break;
        }
    }

    private ArrayList<DataModel> checkIfQueryIsLocalAndGetResponse(Storage.Query query){

        //logic to check if it belongs to current node
        ArrayList<DataModel> arrRespData = MongoDAO.getData("test",new DataModel(query.getKey(),query.getSequenceNo(),null));
        return arrRespData;
    }

    /**
     * Author : n
     * */
    private Storage.Response getResponseMessageForGet(DataModel dataModel){

        Storage.Response.Builder rb = Storage.Response.newBuilder();
        rb.setAction(Storage.Action.GET);
        rb.setSuccess(true);
        rb.setKey(dataModel.getName());
        rb.setSequenceNo(dataModel.getSeqNumber());
        rb.setData(ByteString.copyFrom(dataModel.getDataChunk()));
        return rb.build();
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
                            Global.GlobalCommandMessage clientMessage = (Global.GlobalCommandMessage) msg;

                            hb.setNodeId(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId());
                            hb.setTime(clientMessage.getHeader().getTime());
                            hb.setDestination(clientMessage.getHeader().getDestination());// wont be available in case of request from client. but can be determined based on log replication feature
                            hb.setSourceHost(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId() + "_" + clientMessage.getHeader().getSourceHost());
                            hb.setDestinationHost(clientMessage.getHeader().getSourceHost()); // would be used to return message back to client
                            hb.setMaxHops(3);

                            wb.setHeader(hb);
                            wb.setSecret(1234567809);
                            wb.setPayload(Work.Payload.newBuilder().setQuery(clientMessage.getQuery())); // set the query from client

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
                            wb.setPayload(clientMessage.getPayload()); // set the query from client

                        }
                        if(hb.getMaxHops() > 0) {
                            Work.WorkRequest work = wb.build();
                            edgeQueue.enqueueResponse(work, ei.getChannel());
                            msgDropFlag = false;
                            logger.info("Workmessage pertaining to client request queued");
                        }
                        if (msgDropFlag && globalCommandMessage)
                            logger.info("Message dropped <node,query,source>: <" + ((Global.GlobalCommandMessage) msg).getHeader().getNodeId()
                                    + "," + ((Global.GlobalCommandMessage) msg).getQuery() + "," + ((Global.GlobalCommandMessage) msg).getHeader().getSourceHost() + ">");
                        else if(msgDropFlag && !globalCommandMessage)
                            logger.info("Message dropped <node,query,source>: <" + ((Work.WorkRequest) msg).getHeader().getNodeId()
                                    + "," + ((Work.WorkRequest) msg).getPayload().getQuery() + "," + ((Work.WorkRequest) msg).getHeader().getSourceHost() + ">");

                    }

                }
            } else {// drop the message or queue it for limited time to send to connected node
                //todo
                logger.info("No outbound edges to forward. To be handled");
            }

    }

    private void generateResponseOntoIncomingChannel(GeneratedMessage msg,Storage.Response responseMsg, boolean glabalCommandMessage){

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setTime(System.currentTimeMillis());

        if(glabalCommandMessage){

            Global.GlobalCommandMessage clientMessage = (Global.GlobalCommandMessage) msg;
            Global.GlobalCommandMessage.Builder cb = Global.GlobalCommandMessage.newBuilder(); // message to be returned to actual client

            hb.setNodeId(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId());
            hb.setDestination(clientMessage.getHeader().getDestination());// wont be available in case of request from client. but can be determined based on log replication feature
            hb.setSourceHost(Integer.toString(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId()));
            hb.setDestinationHost(clientMessage.getHeader().getSourceHost()); // would be used to return message back to client

            cb.setHeader(hb);
            cb.setResponse(responseMsg); // set the reponse to the client
            ((PerChannelGlobalCommandQueue) sq).enqueueResponse(cb.build(),null);
        }
        else{
            Work.WorkRequest clientMessage;
            clientMessage = (Work.WorkRequest) msg;
            Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder(); // message to be returned

            hb.setNodeId(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId());

            hb.setDestination(Integer.parseInt(clientMessage.getHeader().getSourceHost().substring(0,clientMessage.getHeader().getSourceHost().indexOf('_'))));// wont be available in case of request from client. but can be determined based on log replication feature
            hb.setSourceHost(clientMessage.getHeader().getSourceHost().substring(clientMessage.getHeader().getSourceHost().indexOf('_')+1));
            hb.setDestinationHost(clientMessage.getHeader().getDestinationHost()); // would be used to return message back to client
            hb.setMaxHops(5);

            wb.setHeader(hb);
            wb.setSecret(1234567809);
            wb.setPayload(Work.Payload.newBuilder().setResponse(responseMsg)); // set the reponse to the client
            ((PerChannelWorkQueue) sq).enqueueResponse(wb.build(),null);
        }
    }

    /**
     * Author : n
     * */
    public Storage.Response getResponseMessageForStore(int result){
        Storage.Response.Builder rb = Storage.Response.newBuilder();
        rb.setAction(Storage.Action.STORE);
        rb.setSuccess(result > 0);
        return rb.build();
    }

}
