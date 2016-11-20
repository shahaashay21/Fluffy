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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;


public class Query extends Resource {

    Common.Request query;

    public Query(ChannelQueue sq){
        super(sq);
    }


    public void handleGlobalCommand(Global.GlobalMessage msg) {

        query = msg.getRequest();
//        logger.info("GGGOOTTT RREEEQQUEESSTTT "+query.getRequestType());
//        logger.info("RREEEQQUEESSTTT iiissss"+query.getFile().getFilename());
            switch (query.getRequestType()) {
                case READ:
                    PrintUtil.printGlobalCommand(msg);
                    try {
                        ArrayList<DataModel> arrRespData = checkIfQueryIsLocalAndGetResponse(query);
//                        if(arrRxespData.size() > 0){
//                            ArrayList<Byte> responsMessage = new ArrayList<>();
                            String responsMessage = "";
                        logger.info("Length of list is: "+ arrRespData.size());

                        //Sorting of messages based on chunkId
//                        Collections.sort(arrRespData, new Comparator<DataModel>() {
//                            @Override
//                            public int compare(DataModel o1, DataModel o2) {
//
//                                return o1.getChunkId() - o2.getChunkId();
//                            }
//                        });

                            //generate a response message
                            for(DataModel dataModel : arrRespData){
//                                responsMessage = responsMessage + dataModel.getData().toString();
                                logger.info("Response message in byte"+ dataModel.getData());
                                logger.info("LENGTH OF FILE IN QUERY IS "+ dataModel.getData().length);
                                Common.Response response = getResponseMessageForGet(dataModel);
                                generateResponseOntoIncomingChannel(msg,response,true);
                            }
//                        byte[] responsMessageByte = arrRespData.get(0).getData();
//                        logger.info("Response message in byte"+ responsMessageByte[1]);
//                        logger.info("LENGTH OF FILE IN QUERY IS "+ responsMessageByte.length);
//                            Common.Response response = getResponseMessageForGet(new DataModel(query.getFile().getFilename(), 0, responsMessageByte));
//                            generateResponseOntoIncomingChannel(msg,response,true);
//                        }else{
//                            forwardRequestOnWorkChannel(msg,true);
//                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case WRITE:
                    PrintUtil.printGlobalCommand(msg);
                    RethinkDAO Users = new RethinkDAO("Users");
                    Users.insertFile(query.getFile().getFilename(), query.getFile().getChunkId(), query.getFile().getChunkCount(), query.getFile().getData().toByteArray());
//                    String fileInserted = (String) Users.insertFile(query.getFile().getFilename(), query.getFile().getChunkId(), query.getFile().getChunkCount(), query.getFile().getData().toByteArray());
//                    System.out.println(fileInserted);
//                    logger.debug("Result of save data in rethink :"+ fileInserted);
                    Common.Response response = getResponseMessageForStore(1);
                    generateResponseOntoIncomingChannel(msg,response,true);

                    break;
                case UPDATE:
                case DELETE:
                    break;
            }

        //}
    }

    public void handleCommand(Pipe.CommandRequest msg) {
        
    }

    public void handleWork(Work.WorkRequest msg) {
        Request query = msg.getPayload().getQuery();
        logger.debug("Query on work channel from " + msg.getHeader().getNodeId());
        switch (query.getRequestType()) {
            case READ:
                PrintUtil.printWork(msg);
                try{
                    ArrayList<DataModel> arrRespData = checkIfQueryIsLocalAndGetResponse(query);
                    if(arrRespData.size() > 0){
                        //generate a response message
                        for(DataModel dataModel : arrRespData){
                            Common.Response response = getResponseMessageForGet(dataModel);
                            generateResponseOntoIncomingChannel(msg,response,false);
                        }
                    }
                    else{
                        forwardRequestOnWorkChannel(msg,false);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case WRITE:
                PrintUtil.printWork(msg);
                RethinkDAO Users = new RethinkDAO("Users");
                String fileInserted = (String) Users.insertFile(query.getFile().getFilename(),query.getFile().getChunkId(), query.getFile().getChunkCount(), query.getFile().getData().toByteArray());
                System.out.println(fileInserted);
                logger.debug("Result of save data in mongo :"+ fileInserted);
                Common.Response response = getResponseMessageForStore(1);
                generateResponseOntoIncomingChannel(msg,response,true);
                break;
            case UPDATE:
            case DELETE:
                break;
        }
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

    private Common.Response getResponseMessageForGet(DataModel dataModel){

        Common.Response.Builder rb = Common.Response.newBuilder();
        rb.setRequestType(RequestType.READ);
        rb.setSuccess(true);
        Common.File.Builder fb = Common.File.newBuilder();
        fb.setFilename(dataModel.getFileName());
        fb.setChunkId(dataModel.getChunkId());
        fb.setData(ByteString.copyFrom(dataModel.getData()));
        fb.setChunkCount(dataModel.getChunkCount());
        rb.setFile(fb);
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

                        Global.GlobalMessage clientMessage = (Global.GlobalMessage) msg;
                        hb.setNodeId(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId());
                        hb.setTime(clientMessage.getGlobalHeader().getTime());
                        hb.setDestination(clientMessage.getGlobalHeader().getDestinationId());// wont be available in case of request from client. but can be determined based on log replication feature
                        hb.setSourceHost(((PerChannelWorkQueue) sq).gerServerState().getConf().getNodeId() + "_" + clientMessage.getGlobalHeader().getClusterId());
                        hb.setDestinationHost(Integer.toString(clientMessage.getGlobalHeader().getDestinationId())); // would be used to return message back to clientMessage
                        hb.setMaxHops(clientMessage.getGlobalHeader().getMaxHops() - 1);

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
        hb.setTime(System.currentTimeMillis());

        if(globalCommandMessage){
            Global.GlobalHeader.Builder ghb = Global.GlobalHeader.newBuilder();
            Global.GlobalMessage clientMessage = (Global.GlobalMessage) msg;
            Global.GlobalMessage.Builder cb = Global.GlobalMessage.newBuilder(); // message to be returned to actual client

            ghb.setClusterId(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId());
            ghb.setDestinationId(clientMessage.getGlobalHeader().getDestinationId());// wont be available in case of request from client. but can be determined based on log replication feature
            ghb.setTime(System.currentTimeMillis());
            //ghb.setClusterId(((PerChannelGlobalCommandQueue) sq).getRoutingConf().getNodeId());
            //ghb.set(clientMessage.getHeader().getSourceHost()); // would be used to return message back to client

            cb.setGlobalHeader(ghb);
            cb.setResponse(responseMsg); // set the reponse to the client
            ((PerChannelGlobalCommandQueue) sq).enqueueResponse(cb.build(),null);
        }
        else{
            hb.setTime(System.currentTimeMillis());
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

    public Common.Response getResponseMessageForStore(int result){
        Common.Response.Builder rb = Common.Response.newBuilder();
        rb.setRequestType(Common.RequestType.WRITE);
        rb.setSuccess(result > 0);
        return rb.build();
    }


}
