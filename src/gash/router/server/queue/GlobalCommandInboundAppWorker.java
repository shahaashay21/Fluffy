/*
 * copyright 2015, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server.queue;

import com.google.protobuf.GeneratedMessage;
<import gash.router.container.GlobalConf;
>mport gash.router.container.RoutingConf;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.WorkInit;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.resources.Ping;
import gash.router.server.resources.Query;
import global.Global;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

public class GlobalCommandInboundAppWorker extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("gciaw:server");

	int workerId;
	PerChannelGlobalCommandQueue sq;
	boolean forever = true;

	private EventLoopGroup group;
	private ChannelFuture channelFuture;

	public GlobalCommandInboundAppWorker(ThreadGroup tgrp, int workerId, PerChannelGlobalCommandQueue sq) {
		super(tgrp, "inboundWork-" + workerId);
		this.workerId = workerId;
		this.sq = sq;
		if (sq.inboundWork == null)
			throw new RuntimeException("connection worker detected null inboundWork queue");
	}

	@Override
	public void run() {
		Channel conn = sq.getChannel();
		if (conn == null || !conn.isOpen()) {
			logger.error("connection missing, no inboundWork communication");
			return;
		}

		while (true) {
			if (!forever && sq.inboundWork.size() == 0)
				break;

			try {
				// block until a message is enqueued
				GeneratedMessage msg = sq.inboundWork.take();
				boolean msgDropFlag;
//				Global.GlobalMessage req1 = ((Global.GlobalMessage) msg);
//
//				if (req1.hasPing()) {
//					System.out.println("Has Pingggggggggggggg");
//					new Ping(sq).handle(req1);
//				}
				// process request and enqueue response
				if(msg instanceof Global.GlobalMessage) {
					//if (((Global.GlobalMessage) msg).getGlobalHeader().getClusterId() == sq.getRoutingConf().getClusterId()) {
						//PrintUtil.printCommand((Pipe.CommandRequest) msg);
						Global.GlobalMessage req = ((Global.GlobalMessage) msg);
						if(verifyLocalOrGlobal(req)) {
							if (req.hasPing()) {
								System.out.println("Has Pingggggggggggggg");
								new Ping(sq).handle(req);
							} else if (req.hasRequest()) {
								new Query(sq).handle(req);
							} else if (req.hasMessage()) {
								logger.info("Message is: " + req.getMessage());
							} else {
								logger.error("Unexpected message type. Yet to handle.");
							}
						}
						else{
							sq.getState().getGemon().pushMessagesIntoCluster(req);
				}
			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected processing failure", e);
				break;
			}
		}

		if (!forever) {
			logger.info("Command incoming connection queue closing");
		}
	}

//	void forwardToClusterRouting(Global.GlobalMessage msg){
//		for(RoutingConf.ClusterRoutingEntry cid : sq.getRoutingConf().getClusterRoutingEntryRouting()){
//			channelInit(cid.getClusterHost(),cid.getClusterPort()).writeAndFlush(msg);
//			//TODO error handle
//		}
//	}
	public boolean verifyLocalOrGlobal(Global.GlobalMessage message){
		//if(((Global.GlobalMessage) msg).getGlobalHeader().getDestinationId())
		boolean check = true;
		for (RoutingConf.RoutingEntry e : sq.getRoutingConf().getRouting()){
			if(e.getId() == message.getGlobalHeader().getDestinationId()){
				check = false;
				break;
			}
		}
		return !check;

	}
	public synchronized Channel channelInit(String host, int port)
	{
		try
		{
			group = new NioEventLoopGroup();
			WorkInit si = new WorkInit(MessageServer.getEmon().getServerState(), false);
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(si);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			channelFuture = b.connect(host, port).syncUninterruptibly();
			//channelFuture.channel().closeFuture().addListener(new EdgeDisconnectionListener(this,ei));

		}
		catch(Throwable ex)
		{
			logger.error("Error initializing channel: " + ex);
			return null;
		}
		return channelFuture.channel();
	}
}