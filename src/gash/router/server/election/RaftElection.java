package gash.router.server.election;

import gash.router.server.MessageServer;
import gash.router.server.ServerState;
import gash.router.server.WorkHandler;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import io.netty.channel.Channel;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import pipe.common.Common;
import pipe.election.*;
import pipe.election.Election;
import pipe.work.Work;

import java.util.List;
import java.util.Random;
import pipe.election.Election.RaftMessage;

/**
 * Created by r on 4/1/16.
 */
public class RaftElection implements gash.router.server.election.Election {
    protected static Logger logger= LoggerFactory.getLogger("Raft");
    private EdgeMonitor emon;
    private int timeElection;
    private long lastKnownBeat = System.currentTimeMillis();
    private Election.RaftMessage votedFor;
    private LogMessage lm = new LogMessage();
    private int nodeId;
    private ServerState state;
    private static int leaderId;
    private ElectionListener listener;
    private int count = 0;
    private boolean appendLogs = false;
    private ElectionState current;
    protected RaftMonitor monitor = new RaftMonitor();

    public enum RState
    {
        Follower,Candidate,Leader
    }
    private int term;
    private RState currentstate;

    public RaftElection()
    {
        this.timeElection = new Random().nextInt(30000);
        if(this.timeElection < 15000)
            this.timeElection += 17000;
        logger.info("Election timeout duration: " + this.timeElection);
        currentstate = RState.Follower;
    }

    public Work.WorkRequest process (Work.WorkRequest workMessage) {
        if (!workMessage.getPayload().hasRaftmsg())
            return null;
        Election.RaftMessage rm = workMessage.getPayload().getRaftmsg();

        Work.WorkRequest msg = null;
        if (rm.getRaftAction().getNumber() == Election.RaftMessage.RaftAction.REQUESTVOTE_VALUE) {
            if (currentstate == RState.Follower || currentstate == RState.Candidate) {
                this.lastKnownBeat = System.currentTimeMillis();

                if ((this.votedFor == null
                        || rm.getTerm() > this.votedFor.getTerm()) &&
                        (rm.getLogIndex() >= this.getLm().getLogIndex())) {
                    if (this.votedFor != null) {
//                        logger.info("Voting for " + workMessage.getHeader().getNodeId()
//                                + " for Term " + rm.getTerm() + " from node "
//                                + nodeId + " voted term "
//                                + this.votedFor.getTerm());
                    }
                    this.votedFor = rm;
                    msg = castvote();
                }
            }
        }
        if (rm.getRaftAction().getNumber() == Election.RaftMessage.RaftAction.WHOISTHELEADER_VALUE) {
			respondToWhoIsTheLeader(workMessage);
        }
        else if (rm.getRaftAction().getNumber() == Election.RaftMessage.RaftAction.LEADER_VALUE) {
            if (rm.getTerm() >= this.term) {
                this.leaderId = workMessage.getHeader().getNodeId();
                this.term = rm.getTerm();
                this.lastKnownBeat = System.currentTimeMillis();
                notifyl(true, workMessage.getHeader().getNodeId());
                logger.info("Node " + workMessage.getHeader().getNodeId() + " is the leader");
            }
        }
        else if (rm.getRaftAction() == RaftMessage.RaftAction.THELEADERIS){
            if (rm.getTerm() >= this.term) {
                this.leaderId = workMessage.getHeader().getNodeId();
                this.term = rm.getTerm();
                this.lastKnownBeat = System.currentTimeMillis();
                notifyl(true, workMessage.getHeader().getNodeId());
                logger.info("Node " + workMessage.getHeader().getNodeId() + " is the leader");
            }
            if(workMessage.getHeader().getDestination()== 100 && workMessage.getHeader().getMaxHops()>0){
            for(Channel ch : MessageServer.getEmon().getAllChannel())
            {
                if(ch != null) {
                    workMessage.toBuilder().getHeader().toBuilder().setMaxHops(workMessage.getHeader().getMaxHops() - 1);

                }
            }

            }
        }
        else if (rm.getRaftAction() == Election.RaftMessage.RaftAction.VOTE) {
            if (currentstate == RState.Candidate) {
                //logger.info("Node " + getNodeId() + " Received vote from Node " + workMessage.getHeader().getNodeId() + " votecount" + count);
                receiveVote(workMessage);
            }
        } else if (rm.getRaftAction().getNumber() == Election.RaftMessage.RaftAction.APPEND_VALUE) {
            leaderId = workMessage.getHeader().getNodeId();
            if (currentstate == RState.Candidate) {
                if (rm.getTerm() >= term) {
                    this.lastKnownBeat = System.currentTimeMillis();
                    this.term = rm.getTerm();
                    this.leaderId = workMessage.getHeader().getNodeId();
                    this.currentstate = RState.Follower;
                    logger.info("Received Append RPC from leader "
                            + workMessage.getHeader().getNodeId());
                }
            }
            else if (currentstate == RState.Follower) {
                leaderId = workMessage.getHeader().getNodeId();
                this.term = rm.getTerm();
                this.lastKnownBeat = System.currentTimeMillis();
//                logger.info("---Leader--- " + workMessage.getHeader().getNodeId()
//                        + "\n RaftAction=" + rm.getRaftAction().getNumber()
//                        + " RaftAppendAction="
//                        + rm.getRaftAppendAction().getNumber());
                if (rm.getRaftAppendAction().getNumber() == Election.RaftMessage.RaftAppendAction.APPENDHEARTBEAT_VALUE) {

//                    logger.info("*Follower stateReceived AppendAction HB RPC from leader "
//                            + workMessage.getHeader().getNodeId()
//                            + "\n RaftAction="
//                            + rm.getRaftAction().getNumber()
//                            + " RaftAppendAction="
//                            + rm.getRaftAppendAction().getNumber());
                }
                else if(rm.getRaftAppendAction().getNumber() == Election.RaftMessage.RaftAppendAction.APPENDLOG_VALUE){
                    List<Election.LogEntries> list = rm.getEntriesList();
                    //Append logs from leader to follower hashmap
                    //from follower's prev
                    for(int i = this.getLm().prevLogIndex+1 ;i < list.size();i++){
                        Election.LogEntries li = list.get(i);
                        int tempindex = li.getLogIndex();
                        String value = li.getLogData();
                        this.getLm().getEntries().put(tempindex, value);
                    }

                }
            }
            if(workMessage.getHeader().getDestination() == 100 && workMessage.getHeader().getMaxHops() > 0)
            {
                for(Channel ch : MessageServer.getEmon().getAllChannel())
                {
                    if(ch != null) {
                        //Election.RaftMessage.Builder rm = Election.RaftMessage.newBuilder();
                        Common.Header.Builder hb = Common.Header.newBuilder();
                        hb.setTime(workMessage.getHeader().getTime());
                        hb.setNodeId(workMessage.getHeader().getNodeId());
                        hb.setMaxHops(workMessage.getHeader().getMaxHops() - 1);

                        //rm.setTerm(term);
                        //rm.setRaftAction(Election.RaftMessage.RaftAction.LEADER);

                        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
                        wb.setHeader(hb);
                        wb.getPayloadBuilder().setRaftmsg(workMessage.getPayload().getRaftmsg());
                        wb.setSecret(12345678);
                        ch.writeAndFlush(wb.build());
                    }
                }
            }
        }
        return msg;
    }
    private void receiveVote(Work.WorkRequest rm) {
        logger.info("Size " + EdgeMonitor.getInstance().getOutboundEdgeInfoList().size());
        if(++count > (EdgeMonitor.getInstance().getOutboundEdgeInfoList().size() + 1) / 2){
            //logger.info("Final count received " + count);
            count = 0;
            currentstate = RState.Leader;
            leaderId = this.nodeId;
            logger.info("Leader Elected " + leaderId);
            notifyl(true,leaderId);
            Common.Header.Builder hb = Common.Header.newBuilder();
            hb.setMaxHops(6);
            hb.setNodeId(rm.getHeader().getNodeId());
            hb.setDestination(100);
            hb.setTime(rm.getHeader().getTime());

            Election.RaftMessage.Builder rf = RaftMessage.newBuilder();
            rf.setRaftAction(RaftMessage.RaftAction.THELEADERIS);

            rm.toBuilder().setHeader(hb);
            rm.getPayload().toBuilder().setRaftmsg(rf);
            for(Channel ch : MessageServer.getEmon().getAllChannel())
            {
                if(ch != null)
                ch.writeAndFlush(rm);
            }
        }
    }

    private Work.WorkRequest sendMessage() {
        Election.RaftMessage.Builder rm = Election.RaftMessage.newBuilder();
        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setTime(System.currentTimeMillis());
        hb.setNodeId(this.nodeId);

        rm.setTerm(term);
        rm.setRaftAction(Election.RaftMessage.RaftAction.LEADER);

        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
        wb.setHeader(hb);
        wb.getPayloadBuilder().setRaftmsg(rm);
        wb.setSecret(12345678);
        return wb.build();
    }

    private void notifyl(boolean b, int nodeId) {
        if(listener != null)
        listener.concludeWith(b,nodeId);
    }

    private synchronized Work.WorkRequest castvote() {
        Election.RaftMessage.Builder rm = Election.RaftMessage.newBuilder();
        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setTime(System.currentTimeMillis());
        hb.setNodeId(this.nodeId);

        //Raft message initialization
        rm.setTerm(term);
        rm.setRaftAction(Election.RaftMessage.RaftAction.VOTE);
        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
        wb.setHeader(hb.build());
        wb.getPayloadBuilder().setRaftmsg(rm.build());
        wb.setSecret(12345678);

        return wb.build();

    }

    public Work.WorkRequest sendAppendNotice() {
        //logger.info("Leader Node " + this.nodeId + " sending appendAction HB RPC's");
        Election.RaftMessage.Builder rm = Election.RaftMessage.newBuilder();
        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setTime(System.currentTimeMillis());
        hb.setNodeId(this.nodeId);
        hb.setDestination(100);
        hb.setMaxHops(600);

        if(this.appendLogs){
            int tempLogIndex = this.lm.getLogIndex();
            rm.setPrevTerm(this.lm.getPrevLogTerm());
            rm.setLogIndex(tempLogIndex);
            rm.setPrevlogIndex(this.lm.getPrevLogIndex());

            for (Integer key : this.getLm().getEntries().keySet())
            {
                Election.LogEntries.Builder le = Election.LogEntries.newBuilder();
                String value = this.getLm().getEntries().get(key);
                le.setLogIndex(key);
                le.setLogData(value);
                rm.setEntries(key, le);
            }
            rm.setRaftAppendAction(Election.RaftMessage.RaftAppendAction.APPENDLOG);
            this.appendLogs = false;
        }
        else{
            rm.setRaftAppendAction(Election.RaftMessage.RaftAppendAction.APPENDHEARTBEAT);
        }

        rm.setTerm(term);
        rm.setRaftAction(Election.RaftMessage.RaftAction.APPEND);
        // Raft Message to be added
        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
        wb.setHeader(hb.build());
        wb.getPayloadBuilder().setRaftmsg(rm);
        return wb.build();
        //}
    }

    private void startElection(){
       // logger.info("Time Out!  Election declared by node: " + getNodeId() + " For Term " + (term+1));

        lastKnownBeat = System.currentTimeMillis();
        currentstate = RState.Candidate;
        count = 1;
        term++;
        if (EdgeMonitor.getInstance().getOutboundEdgeInfoList().size() == 0) {
            notifyl(true, this.nodeId);
            count = 0;
            currentstate = RState.Leader;
            leaderId = this.nodeId;
            //logger.info(" Leader elected " + this.nodeId);
            for(Channel ch : MessageServer.getEmon().getAllChannel())
            {
                if(ch != null)
                    ch.writeAndFlush(sendMessage());
            }
        }

        else {
            /*for(EdgeInfo ei : EdgeMonitor.getInstance().getOutboundEdgeInfoList())
            {
                ei.getChannel().writeAndFlush(sendRequestVoteNotice());
            }*/
            for(Channel ch : MessageServer.getEmon().getAllChannel())
            {
                if(ch != null)
                    ch.writeAndFlush(sendRequestVoteNotice());
            }
        }

    }

    private Work.WorkRequest sendRequestVoteNotice() {
        Election.RaftMessage.Builder rm = RaftMessage.newBuilder();
        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setTime(System.currentTimeMillis());
        hb.setNodeId(this.nodeId);

        // Raft Message to be added
        rm.setTerm(term);
        rm.setRaftAction(RaftMessage.RaftAction.REQUESTVOTE);
        rm.setLogIndex(this.getLm().getLogIndex());
        rm.setPrevlogIndex(this.getLm().getPrevLogIndex());
        rm.setPrevTerm(this.getLm().getPrevLogTerm());


        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
        wb.setHeader(hb.build());
        wb.getPayloadBuilder().setRaftmsg(rm.build());
        wb.setSecret(12345678);
        return wb.build();
    }

    public class RaftMonitor extends Thread{
        public void run(){
            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (currentstate == RState.Leader)
                    /*for(EdgeInfo ei : EdgeMonitor.getInstance().getOutboundEdgeInfoList()) {
                        ei.getChannel().writeAndFlush(sendAppendNotice());
                    }*/
                    for(Channel ch : MessageServer.getEmon().getAllChannel())
                    {
                        if(ch != null)
                            ch.writeAndFlush(sendAppendNotice());
                    }
                else {
                    boolean blnStartElection = RaftManager.getInstance()
                            .assessCurrentState();
                    if (blnStartElection) {
                        long now = System.currentTimeMillis();
                        if ((now - lastKnownBeat) > timeElection)
                            startElection();
                    }
                }
            }
        }
    }

    private void respondToWhoIsTheLeader(Work.WorkRequest msg) {
        if (this.leaderId == 0) {
            logger.info("----> I cannot respond to who the leader is! I don't know!");
            return;
        }
        logger.info("Node " + this.nodeId + " is replying to "
                + msg.getHeader().getNodeId()
                + "'s request who the leader is. Its Node " + this.leaderId);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(this.nodeId);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(600);
        hb.setDestination(100);

        pipe.election.Election.RaftMessage.Builder rmb = pipe.election.Election.RaftMessage.newBuilder();
        rmb.setLeader(this.leaderId);
        rmb.setRaftAction(pipe.election.Election.RaftMessage.RaftAction.THELEADERIS);

        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
        wb.setHeader(hb.build());
        wb.getPayloadBuilder().setRaftmsg(rmb);
        for(Channel ch : MessageServer.getEmon().getAllChannel())
        {
            if(ch != null)
                ch.writeAndFlush(wb.build());
        }

    }

    public RState getCurrentState() {
        return currentstate;
    }

    public void setCurrentState(RState currentState) {
        this.currentstate = currentState;
    }

    public static int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    public int getTimeElection() {
        return timeElection;
    }

    public void setTimeElection(int timeElection) {
        this.timeElection = timeElection;
    }

    public void setListener(ElectionListener listener) {
        this.listener = listener;
    }

    public Integer getElectionId() {
        if (current == null)
            return null;
        return current.electionID;
    }

    @Override
    public Integer createElectionID() {
        return ElectionIDGenerator.nextID();
    }

    @Override
    public Integer getWinner() {
       if (current == null)
            return null;
        else if (current.state.getNumber() == Election.LeaderElection.ElectAction.DECLAREELECTION_VALUE)
            return current.candidate;
        else
            return null;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public synchronized void clear() {
        current = null;
    }

    public boolean isElectionInprogress() {
        return current != null;
    }

    public RaftMonitor getMonitor() {
        return monitor;
    }
    public LogMessage getLm() {
        return lm;
    }

    public void setLm(LogMessage lm) {
        this.lm = lm;
    }

    public boolean isAppendLogs() {
        return appendLogs;
    }

    public void setAppendLogs(boolean appendLogs) {
        this.appendLogs = appendLogs;
    }

}
