package gash.router.server.resources;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import database.dao.RethinkDAO;
import database.model.DataModel;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.queue.ChannelQueue;
import gash.router.server.queue.PerChannelGlobalCommandQueue;
import gash.router.server.queue.PerChannelWorkQueue;
import global.Global;
import org.json.simple.JSONObject;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;
import pipe.common.Common.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Query extends Resource {

    Common.Request query;

    public Query(ChannelQueue sq){
        super(sq);
    }

    public Query(){};


    public void handleGlobalCommand(Global.GlobalMessage msg) {

        query = msg.getRequest();
        logger.info("GGGOOTTT RREEEQQUEESSTTT "+query.getRequestType());
        logger.info("RREEEQQUEESSTTT iiissss"+query.getFile().getFilename());
            switch (query.getRequestType()) {
                case READ:
                    PrintUtil.printGlobalCommand(msg);
                    try {
                        ArrayList<DataModel> arrRespData = checkIfQueryIsLocalAndGetResponse(query);
                        logger.info("Length of list is: "+ arrRespData.size());

                        if(arrRespData.size() > 0) {
                            //generate a response message
                            for (DataModel dataModel : arrRespData) {
                                logger.info("Response message in byte" + dataModel.getData());
                                logger.info("LENGTH OF FILE IN QUERY IS " + dataModel.getData().length);
                                Common.Response response = getResponseMessageForGet(dataModel, query.getRequestId(), true);
                                generateResponseOntoIncomingChannel(msg, response, true);
                            }
                        }else{
                            forwardRequestOnWorkChannel1(msg, false, query.getFile());
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case WRITE:
                    PrintUtil.printGlobalCommand(msg);
                    RethinkDAO Users = new RethinkDAO("Users");
                    Integer answer = Users.insertFile(query.getFile().getFilename(), query.getFile().getChunkId(), query.getFile().getTotalNoOfChunks(), query.getFile().getData().toByteArray());
                    if (answer > 0){
                        Common.Response response = getResponseMessageForStore(1,query.getRequestId(), true);
                        generateResponseOntoIncomingChannel(msg,response,true);
                    }else{
                        forwardRequestOnWorkChannel1(msg, false, query.getFile());
                    }
                    break;
                case UPDATE:
                    RethinkDAO UsersUpdate = new RethinkDAO("Users");


                    break;
                case DELETE:
                    RethinkDAO users = new RethinkDAO("Users");
                    JSONObject data = new JSONObject();
                    data.put("fileName", query.getFile().getFilename());

                    Integer deleted = users.deleteFile(data);
                    if(deleted > 0){
                        Common.Response responseDelete = getResponseMessageForDelete(1, query.getRequestId(),true);
                        generateResponseOntoIncomingChannel(msg,responseDelete,true);
                    }else {
                        forwardRequestOnWorkChannel1(msg, false, query.getFile());
                    }
//                    forwardRequestOnWorkChannel1(msg, false, query.getFile());
                    break;
            }

        //}
    }

    public void handleCommand(Pipe.CommandRequest msg) {
        
    }

    public void handleWork(Work.WorkRequest msg) {
        System.out.println("HAS BROADCAST: "+ msg.hasBroadCast());
        System.out.println("BROADCAST VALUE: "+ msg.getBroadCast());
        System.out.println("REQUEST TYPE VALUE: "+ msg.getPayload().getQuery().getRequestType());
        Request query = msg.getPayload().getQuery();
        if(msg.hasBroadCast() && msg.getBroadCast()) {
            logger.debug("Query on work channel from " + msg.getHeader().getNodeId());
            switch (query.getRequestType()) {
                case READ:
                    PrintUtil.printWork(msg);
                    try {
                        Common.Response response = null;
                        ArrayList<DataModel> arrRespData = checkIfQueryIsLocalAndGetResponse(query);
                        if (arrRespData.size() > 0) {
                            //generate a response message
                            for (DataModel dataModel : arrRespData) {
//                                responsMessage = responsMessage + dataModel.getData().toString();
                                logger.info("Response message in byte" + dataModel.getData());
                                logger.info("LENGTH OF FILE IN QUERY IS " + dataModel.getData().length);
                                response = getResponseMessageForGet(dataModel, query.getRequestId(), true);
                            }
                        } else {
                            response = getResponseMessageForGet(null, query.getRequestId(), false);
                        }
                        generateResponseOntoIncomingChannel(msg, response, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WRITE:
                    Common.Response response = null;
                    PrintUtil.printWork(msg);
                    RethinkDAO Users = new RethinkDAO("Users");
                    Integer answer = Users.insertFile(query.getFile().getFilename(), query.getFile().getChunkId(), query.getFile().getTotalNoOfChunks(), query.getFile().getData().toByteArray());
                    if (answer > 0){
                        response = getResponseMessageForStore(1, query.getRequestId(), true);
                    }else{
                        response = getResponseMessageForStore(1, query.getRequestId(), false);
                    }
//                    Global.GlobalMessage gm = workToGlobalRequest(msg, msg.getFile());
                    generateResponseOntoIncomingChannel(msg,response,false);
                    break;
                case UPDATE:
                case DELETE:
                    RethinkDAO users = new RethinkDAO("Users");
                    JSONObject data = new JSONObject();
                    data.put("fileName", query.getFile().getFilename());

                    Integer deleted = users.deleteFile(data);
                    if(deleted > 0){
                        Common.Response responseDelete = getResponseMessageForDelete(1, query.getRequestId(), true);
                        generateResponseOntoIncomingChannel(msg,responseDelete,true);
                    }else {
                        Common.Response responseDelete = getResponseMessageForDelete(1, query.getRequestId(), true);
                        generateResponseOntoIncomingChannel(msg,responseDelete,false);
                    }
                    break;
            }
        }
    }

//    public Work.WorkRequest globalToWork(Global.GlobalMessage msg, File newFile){
//
//        Header.Builder hb = createHeader(msg.getGlobalHeader().getClusterId(), msg.getGlobalHeader().getDestinationId());
//
//        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
//        wb.setHeader(hb);
//        Work.Payload.Builder wPayload = Work.Payload.newBuilder();
//        wPayload.setQuery(((Global.GlobalMessage) msg).getRequest());
//
//
//        wb.setPayload(wPayload);
//        wb.setFile(newFile);
//        wb.setSecret(12345678);
//
//        return wb.build();
//    }

    public Global.GlobalMessage workToGlobalRequest(Work.WorkRequest msg, File newFile){
        Global.GlobalHeader.Builder hb = createGlobalHeader(999, msg.getHeader().getDestination());
        Global.GlobalMessage.Builder gmb = Global.GlobalMessage.newBuilder();
        gmb.setGlobalHeader(hb);
        gmb.setRequest(msg.getPayload().getQuery());

        return gmb.build();
    }

    public Global.GlobalMessage workToGlobalResponse(Work.WorkRequest msg, File newFile){
        Global.GlobalHeader.Builder hb = createGlobalHeader(999, msg.getHeader().getDestination());
        Global.GlobalMessage.Builder gmb = Global.GlobalMessage.newBuilder();
        gmb.setGlobalHeader(hb);
        gmb.setResponse(msg.getPayload().getResponse());

        return gmb.build();
    }

    private ArrayList<DataModel> checkIfQueryIsLocalAndGetResponse(Common.Request query) throws IOException {
//        logger.info("iinnntttooo ttthhheee checkIfQueryIsLocalAndGetResponse"+ query.getFile().getFilename());
        //logic to check if it belongs to current node
        RethinkDAO users = new RethinkDAO("Users");
        JSONObject fileNameFilter = new JSONObject();
        fileNameFilter.put("fileName", query.getFile().getFilename());
        ArrayList<DataModel> arrRespData = users.fetchFile(fileNameFilter);
        return arrRespData;
    }

    private Common.Response getResponseMessageForGet(DataModel dataModel, String reqId, Boolean suc ){

        if(dataModel == null){
            Common.Response.Builder rb = Common.Response.newBuilder();
            rb.setRequestType(RequestType.READ);
            rb.setSuccess(suc);
            rb.setRequestId(reqId);
            Common.Failure.Builder failure = Common.Failure.newBuilder();
            failure.setId(111);
            Common.File.Builder fb = Common.File.newBuilder();
            rb.setFailure(failure);
            return rb.build();
        }else{
            Common.Response.Builder rb = Common.Response.newBuilder();
            rb.setRequestType(RequestType.READ);
            rb.setSuccess(suc);
            rb.setRequestId(reqId);
            Common.File.Builder fb = Common.File.newBuilder();
            fb.setFilename(dataModel.getFileName());
            fb.setChunkId(dataModel.getChunkId());
            fb.setData(ByteString.copyFrom(dataModel.getData()));
            fb.setTotalNoOfChunks(dataModel.getChunkCount());
            rb.setFile(fb);
            return rb.build();
        }
    }

    //Based on last protbuf change
    public static boolean broadCast = false;
    public static HashMap<String, Integer> broadCastMap = new HashMap<>();
    public static int broadcastNodes = 0;
    private void forwardRequestOnWorkChannel1(GeneratedMessage msg, boolean forwardToGlobal, File newFile){
        if(forwardToGlobal){
            //////TODOO SEND MSG TO NEEL'S GLOBAL FORWARD
            ((PerChannelGlobalCommandQueue)sq).getState().getGemon().pushMessagesIntoCluster((Global.GlobalMessage) msg);
        }else {
            broadCast = true;
            broadcastNodes = 0;
            for (EdgeInfo ei : MessageServer.getEmon().getOutboundEdgeInfoList()) {
                if (ei.getChannel() != null && ei.isActive()) {
                    String fileName = ((Global.GlobalMessage) msg).getResponse().getFile().getFilename();
                    Work.WorkState.Builder sb = Work.WorkState.newBuilder();
                    sb.setEnqueued(-1);
                    sb.setProcessed(-1);


                    Header.Builder hb = createHeader(((Global.GlobalMessage) msg).getGlobalHeader().getClusterId(), ((Global.GlobalMessage) msg).getGlobalHeader().getDestinationId());

                    Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
                    wb.setHeader(hb);
                    Work.Payload.Builder wPayload = Work.Payload.newBuilder();
                    wPayload.setQuery(((Global.GlobalMessage) msg).getRequest());


                    wb.setPayload(wPayload);
                    wb.setFile(newFile);
                    wb.setSecret(12345678);
                    wb.setBroadCast(true);
                    Work.WorkRequest check = wb.build();
                    System.out.println("IS FILE" + check.hasFile());
                    System.out.println("IS FILE" + check.getFile().getFilename());
                    System.out.println("Forwarding to other nodes.");
                    ei.getChannel().writeAndFlush(check);
                    broadcastNodes++;


                }
            }
            broadCastMap.put(((Global.GlobalMessage) msg).getRequest().getRequestId(), broadcastNodes);
        }
    }

    public static Common.Header.Builder createHeader(int cluster_id, int destination_id) {
        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setTime(System.currentTimeMillis());
        hb.setDestination(destination_id);
        hb.setNodeId(cluster_id);
        return hb;
    }

    public static Global.GlobalHeader.Builder createGlobalHeader(int cluster_id, int distination_id) {
        Global.GlobalHeader.Builder hb = Global.GlobalHeader.newBuilder();
        hb.setTime(System.currentTimeMillis());
        hb.setDestinationId(distination_id);
        hb.setClusterId(cluster_id);
        return hb;
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
                        hb.setNodeId(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId());
                        hb.setTime(clientMessage.getGlobalHeader().getTime());
                        hb.setDestination(clientMessage.getGlobalHeader().getDestinationId());// wont be available in case of request from client. but can be determined based on log replication feature
                        hb.setSourceHost(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId() + "_" + clientMessage.getGlobalHeader().getClusterId());
                        hb.setDestinationHost(Integer.toString(clientMessage.getGlobalHeader().getDestinationId())); // would be used to return message back to clientMessage
                        //hb.setMaxHops(clientMessage.getGlobalHeader().getMaxHops() - 1);

                        wb.setHeader(hb);
                        wb.setSecret(1234567809);

                        Work.Payload.Builder wp = Work.Payload.newBuilder();
                        if(clientMessage.hasRequest()){
                            wb.setPayload(wp.setQuery(clientMessage.getRequest()));
                        }else if(clientMessage.hasResponse()){
                            wb.setPayload(wp.setResponse(clientMessage.getResponse()));
                        }


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
                        logger.info("Message dropped <node,query,source>: <" + ((Global.GlobalMessage) msg).getGlobalHeader().getClusterId()
                                + "," + ((Global.GlobalMessage) msg).getRequest() + "," + ((Global.GlobalMessage) msg).getGlobalHeader().getClusterId() + ">");
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

    private void generateResponseOntoIncomingChannel(GeneratedMessage msg, Common.Response responseMsg, boolean globalCommandMessage){

        Common.Header.Builder hb = Common.Header.newBuilder();

        if(globalCommandMessage){

            Global.GlobalHeader.Builder ghb = Global.GlobalHeader.newBuilder();
            Global.GlobalMessage clientMessage = (Global.GlobalMessage) msg;
            Global.GlobalMessage.Builder cb = Global.GlobalMessage.newBuilder(); // message to be returned to actual client

            ghb.setClusterId(clientMessage.getGlobalHeader().getClusterId());
            ghb.setDestinationId(clientMessage.getGlobalHeader().getDestinationId());// wont be available in case of request from client. but can be determined based on log replication feature
            ghb.setTime(System.currentTimeMillis());
            //ghb.setClusterId(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId());
            //ghb.set(clientMessage.getHeader().getSourceHost()); // would be used to return message back to client

            cb.setGlobalHeader(ghb);
            cb.setResponse(responseMsg); // set the reponse to the client
//            if(msg.get)
            ((PerChannelGlobalCommandQueue) sq).enqueueResponse(cb.build(),null);
        }
        else{
            Work.WorkRequest clientMessage;
            clientMessage = (Work.WorkRequest) msg;
            Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder(); // message to be returned

//            hb.setNodeId(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId());
            hb.setTime(((Work.WorkRequest) msg).getHeader().getTime());
            hb.setNodeId(((Work.WorkRequest) msg).getHeader().getNodeId());
            hb.setDestination(((Work.WorkRequest) msg).getHeader().getDestination());
//            hb.setTime(System.currentTimeMillis());
//            Work.WorkRequest clientMessage;
//            clientMessage = (Work.WorkRequest) msg;
//            Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder(); // message to be returned
//
////            hb.setNodeId(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId());
//
//            hb.setDestination(((Work.WorkRequest) msg).getHeader().getDestination());
//            hb.setDestination(Integer.parseInt(clientMessage.getHeader().getSourceHost().substring(0,clientMessage.getHeader().getSourceHost().indexOf('_'))));// wont be available in case of request from client. but can be determined based on log replication feature
//            hb.setSourceHost(clientMessage.getHeader().getSourceHost().substring(clientMessage.getHeader().getSourceHost().indexOf('_')+1));
//            hb.setDestinationHost(clientMessage.getHeader().getDestinationHost()); // would be used to return message back to client
            hb.setMaxHops(5);

            wb.setHeader(hb);
            wb.setSecret(1234567809);
            wb.setBroadCast(false);
            Work.Payload.Builder wp = Work.Payload.newBuilder();
//            wp.setResponse()
            wb.setPayload(Work.Payload.newBuilder().setResponse(responseMsg)); // set the reponse to the client
            ((PerChannelWorkQueue) sq).enqueueResponse(wb.build(),null);
        }
    }

    public Common.Response getResponseMessageForStore(int result, String reqId, Boolean suc){
        Common.Response.Builder rb = Common.Response.newBuilder();
        rb.setRequestType(Common.RequestType.WRITE);
        rb.setRequestId(reqId);
        rb.setSuccess(suc);
        return rb.build();
    }

    public Common.Response getResponseMessageForDelete(int result, String reqId, Boolean suc){
        Common.Response.Builder rb = Common.Response.newBuilder();
        rb.setRequestType(Common.RequestType.DELETE);
        rb.setRequestId(reqId);
        rb.setSuccess(suc);
        return rb.build();
    }


}
