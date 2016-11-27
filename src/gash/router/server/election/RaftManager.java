package gash.router.server.election;

import database.model.DataModel;
import gash.router.container.ClusterConfList;
import gash.router.container.RoutingConf;
import gash.router.server.CommandInit;
import gash.router.server.MessageServer;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;

import java.beans.Beans;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class RaftManager implements ElectionListener{
    protected static Logger logger = LoggerFactory.getLogger("Election Officer");
    protected static AtomicReference<RaftManager> instance = new AtomicReference<RaftManager>();

    private static RoutingConf conf;
    private static long lastKnownBeat = System.currentTimeMillis();
    //private static ClusterConfList clusterConf;
    public static HashMap<Integer,HashSet<DataModel>> clusterFileInfo = new HashMap<>();

    private int firstTime = 2;
    private Election election;
    private int electionCycle = -1;
    private Integer syncPt = 1;
    Integer leaderNode;

    public static RaftManager initManager(RoutingConf conf)
    {
        RaftManager.conf =conf;
        //instance.compareAndSet(null,new RaftManager());
        instance.compareAndSet(null,new RaftManager());
        return instance.get();
    }

    public static RaftManager getInstance() {
        return instance.get();
    }

    public void processRequest(Work.WorkRequest msg)
    {
        if (!msg.getPayload().hasRaftmsg())
            return;
        pipe.election.Election.RaftMessage rm = msg.getPayload().getRaftmsg();
        if (rm.getRaftAction().getNumber() == pipe.election.Election.RaftMessage.RaftAction.WHOISTHELEADER_VALUE) {
            respondToWhoIsTheLeader(msg);
            return;
        }
        // else respond to the message using process Function in RaftElection

        Work.WorkRequest rtn = electionInstance().process(msg);
        if (rtn != null)
        {
          
            for(Channel ch : MessageServer.getEmon().getAllChannel())
            {
                if(ch!=null)
                    ch.writeAndFlush(rtn);
            }
        }
    }

    public boolean assessCurrentState() {
        if (firstTime > 0 && EdgeMonitor.getInstance().getOutboundEdgeInfoList().size() > 0) {
            // give it two tries to get the leader
            this.firstTime--;
            askWhoIsTheLeader();
            return false;
        } else if (leaderNode == null && (election == null || !election.isElectionInprogress())) {
            // if this is not an election state, we need to assess the H&S of
            // the network's leader
            synchronized (syncPt) {
                long now = System.currentTimeMillis();
                if(now-lastKnownBeat>1000)
                    return true;
            }
        }
        return false;
    }

    private void askWhoIsTheLeader() {
        //logger.info("Node " + conf.getNodeId() + " is searching for the leader");
        if (whoIsTheLeader() == null) {
            //logger.info("----> I cannot find the leader is! I don't know!");
            return;
        } else {
            logger.info("The Leader is " + this.leaderNode);
        }

    }

    public Integer whoIsTheLeader() {
            return this.leaderNode;
    }

    public Election electionInstance() {
        if (election == null) {
            synchronized (syncPt) {
                if (election != null)
                    return election;

                // new election
                String clazz = RaftManager.conf.getElectionImplementation();
                System.out.println("Clazz "+this.getClass().getClassLoader() + clazz);
                // if an election instance already existed, this would
                // override the current election
                try {
                    election = (Election) Beans.instantiate(this.getClass()
                            .getClassLoader(), clazz);
                    election.setNodeId(conf.getNodeId());
                    election.setListener(this);
                } catch (Exception e) {
                    logger.error("Failed to create " + clazz, e);
                }
            }
        }

        return election;

    }

    private void respondToWhoIsTheLeader(Work.WorkRequest msg) {
        if (this.leaderNode == null) {
            //logger.info("----> I cannot respond to who the leader is! I don't know!");
            return;
        }
//        logger.info("Node " + conf.getNodeId() + " is replying to "
//                + msg.getHeader().getNodeId()
//                + "'s request who the leader is. Its Node " + this.leaderNode);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(conf.getNodeId());
        hb.setTime(System.currentTimeMillis());

        pipe.election.Election.RaftMessage.Builder rmb = pipe.election.Election.RaftMessage.newBuilder();
        rmb.setLeader(this.leaderNode);
        rmb.setRaftAction(pipe.election.Election.RaftMessage.RaftAction.THELEADERIS);

        Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();
        wb.setHeader(hb.build());
        wb.getPayloadBuilder().setRaftmsg(rmb);
        writeAndFlush(wb.build());

    }

    private void writeAndFlush(Work.WorkRequest msg) {
        for(Channel ch : MessageServer.getEmon().getAllChannel())
        {
            if(ch != null)
            ch.writeAndFlush(msg);
        }
    }


    public void startMonitor(ServerState state) {
        //logger.info("Raft Monitor Started ");
        if (election == null)
            ((RaftElection) electionInstance()).getMonitor().start();

        //new Thread(new EdgeMonitor(state)).start();
    }

    @Override
    public void concludeWith(boolean success, Integer LeaderID) {
        if (success) {
            logger.info("----> the leader is " + LeaderID);
            this.leaderNode = LeaderID;
        }

        election.clear();
    }

    public void createLogs(String imageName)
    {
        LogMessage lm = ((RaftElection) electionInstance()).getLm();
        Integer logIndex = lm.getLogIndex();
        LinkedHashMap<Integer, String> entries = lm.getEntries();
        lm.setPrevLogIndex(logIndex);
        lm.setLogIndex(++logIndex);
        entries.put(logIndex,imageName);
        lm.setEntries(entries);
        ((RaftElection) electionInstance()).setAppendLogs(true);
    }

    public void askForFiles(){
      
    }

    public boolean checkIfFileIsInCluster(DataModel data){

        boolean flag = false;
        Integer nodeId=0;
        for(Map.Entry<Integer,HashSet<DataModel>> entry: clusterFileInfo.entrySet() ){
            if(entry.getValue().contains(data)){
                flag = true;
                nodeId = entry.getKey();
                break;
            }
        }

        return flag;

    }


    //Setters and Getters
    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        RaftManager.logger = logger;
    }

    public static void setInstance(AtomicReference<RaftManager> instance) {
        RaftManager.instance = instance;
    }

    public static RoutingConf getConf() {
        return conf;
    }

    public static void setConf(RoutingConf conf) {
        RaftManager.conf = conf;
    }

    public static long getLastKnownBeat() {
        return lastKnownBeat;
    }

    public static void setLastKnownBeat(long lastKnownBeat) {
        RaftManager.lastKnownBeat = lastKnownBeat;
    }

    public int getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(int firstTime) {
        this.firstTime = firstTime;
    }

    public Election getElection() {
        return election;
    }

    public void setElection(Election election) {
        this.election = election;
    }

    public int getElectionCycle() {
        return electionCycle;
    }

    public void setElectionCycle(int electionCycle) {
        this.electionCycle = electionCycle;
    }

    public Integer getSyncPt() {
        return syncPt;
    }

    public void setSyncPt(Integer syncPt) {
        this.syncPt = syncPt;
    }

    public Integer getLeaderNode() {
        return leaderNode;
    }

    public void setLeaderNode(Integer leaderNode) {
        this.leaderNode = leaderNode;
    }
}
