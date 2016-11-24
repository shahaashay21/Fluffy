package gash.router.server.edges;

import gash.router.container.GlobalConf;
import gash.router.server.GlobalCommandInit;
import gash.router.server.ServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class GlobalEdgeMonitor {

	private static Logger logger = LoggerFactory.getLogger("global edge monitor");
	private ServerState state;
	private GlobalEdgeList globalOutboud = new GlobalEdgeList();
	
	public GlobalEdgeMonitor(ServerState state) {
		this.state = state;
		
		if (state.getGlobalConf().getRouting() != null) {
			for (GlobalConf.GlobalRoutingEntry e : state.getGlobalConf().getRouting()) {
				globalOutboud.addNode(e.getHost(), e.getClusterId(), e.getPort());
			}
		}
	}
	
	public void pushMessagesIntoCluster(GlobalMessage global){
		for(GlobalEdgeInfo ei: globalOutboud.map.values()) {
			if(ei.getChannel() != null && ei.isActive()){
				ei.getChannel().writeAndFlush(global);
				break;
			}
			else{
				try{
					logger.info("trying to connect to cluster node " + ei.getRef());
					EventLoopGroup group = new NioEventLoopGroup();
					GlobalCommandInit si = new GlobalCommandInit(state);
					Bootstrap b = new Bootstrap();
					b.group(group).channel(NioSocketChannel.class).handler(si);
					b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
					b.option(ChannelOption.TCP_NODELAY, true);
					b.option(ChannelOption.SO_KEEPALIVE, true);
					
					ChannelFuture channel = b.connect(ei.getRef(), ei.getPort()).syncUninterruptibly();
					
					ei.setChannel(channel.channel());
					ei.setActive(channel.channel().isActive());
				}
				catch(Exception e){
					logger.error("Failed connecting to other cluster node "+ei.getRef()+" , i'm clueless");
				}
			}
		}
	}

//	public void forwardRequest(String id, CommandMessage msg) {
//		GlobalMessage.Builder wm = GlobalMessage.newBuilder();
//		GlobalHeader.Builder header = GlobalHeader.newBuilder();
//		header.setTime(System.currentTimeMillis());
//		header.setDestinationId(0);
//		header.setClusterId(state.getGlobalConf().getClusterId());
//		wm.setGlobalHeader(header);
//		Request.Builder request = Request.newBuilder();
//		request.setRequestId(id);
//		File.Builder file = File.newBuilder();
//		logger.info("file name decoded as "+msg.getReqMsg().getKey());
//		file.setFilename(msg.getReqMsg().getKey());
//		request.setFile(file);
//		request.setRequestType(RequestType.READ);
//		wm.setRequest(request);
//
//		for(EdgeInfo ei : globalOutboud.map.values()){
//			if(ei.getChannel() != null && ei.isActive()){
//				ei.getChannel().writeAndFlush(wm.build());
//			}
//			else{
//				try{
//					logger.info("trying to connect to node " + ei.getRef());
//					EventLoopGroup group = new NioEventLoopGroup();
//					GlobalInit si = new GlobalInit(state, false);
//					Bootstrap b = new Bootstrap();
//					b.group(group).channel(NioSocketChannel.class).handler(si);
//					b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
//					b.option(ChannelOption.TCP_NODELAY, true);
//					b.option(ChannelOption.SO_KEEPALIVE, true);
//
//					ChannelFuture channel = b.connect(ei.getHost(), ei.getPort()).syncUninterruptibly();
//					if(channel != null) {
//						ei.setChannel(channel.channel());
//						ei.setActive(channel.channel().isActive());
//						channel.channel().writeAndFlush(wm.build());
//					}
//				}
//				catch(Exception e){
//					logger.info("Error connecting to the other lcuster node");
//				}
//			}
//		}
//	}

	public void setConf(GlobalConf globalConf) {
		// TODO Auto-generated method stub
		
	}
	
}
