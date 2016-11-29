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
import gash.router.container.GlobalConf;
import gash.router.container.RoutingConf;
import gash.router.server.GlobalCommandHandler;
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.WorkInit;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.election.RaftElection;
import gash.router.server.election.RaftManager;
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

				if(msg instanceof Global.GlobalMessage) {

					Global.GlobalMessage req = ((Global.GlobalMessage) msg);
					System.out.println("Got A global message request type"+ req.getRequest().getRequestType());
					System.out.println("Got A global message response type"+ req.getResponse().getRequestType());
					System.out.println("Inbound work queue size " + sq.inboundWork.size());


					//Has Ping
					if (req.hasPing()) {
						if (verifyLocalOrGlobal(req)) {
							if (req.getGlobalHeader().getDestinationId() == sq.getState().getConf().getNodeId()) {
								System.out.println("Has Pingggggggggggggg");
								new Ping(sq).handle(req);
							} else {
								for (EdgeInfo ei : sq.getState().getEmon().getOutboundEdgeInfoList()) {
									if (ei.getRef() == req.getGlobalHeader().getDestinationId()) {
										if (ei.getChannel().isActive()) {
											ei.getChannel().writeAndFlush(req);
										}
									}
								}
							}
						} else {
							sq.getState().getGemon().pushMessagesIntoCluster(req);
						}
					}


					//Has Request
					else if (req.hasRequest()) {
						System.out.println("GOT REQUEST FROM OTHER CLUSTER CLIENT/NODE");
						if (checkIfLeader()) {
							System.out.println("I AM LEADER");
							if (checkClusterIdForOurRequest(req)) {
								System.out.println("ROUND TRIP FOR MY CLUSTER");
								Common.Failure.Builder cf = Common.Failure.newBuilder();
								cf.setId(1);
								cf.setMessage("File " + req.getRequest().getFile().getFilename() + " not found.");

								Common.Response.Builder crb = Common.Response.newBuilder();
								crb.setRequestId(req.getRequest().getRequestId());
								crb.setRequestType(req.getRequest().getRequestType());
								crb.setSuccess(false);
								crb.setFailure(cf);

								Global.GlobalHeader.Builder ghb = Global.GlobalHeader.newBuilder();
								ghb.setClusterId(sq.getState().getGlobalConf().getClusterId());
								ghb.setTime(req.getGlobalHeader().getTime());

								Global.GlobalMessage.Builder gm = Global.GlobalMessage.newBuilder();
								gm.setGlobalHeader(ghb);
								gm.setResponse(crb);
								if (GlobalCommandHandler.globalClientChannel.containsKey(req.getRequest().getRequestId())) {
									Channel res = GlobalCommandHandler.globalClientChannel.get(req.getRequest().getRequestId());
									//GlobalCommandHandler.globalClientChannel.remove(msg.getPayload().getResponse().getRequestId());
									System.out.println("SENT BACK TO CLIENT");
									res.writeAndFlush(gm);
								} else {
									logger.info("Request ID not found in Hashmap for Request - RequestId:" + req.getRequest().getRequestId());
								}
//								new Query(sq).handle(req);
							} else if (verifyLocalOrGlobal(req)) {
								System.out.println("NOT FOR MY CLUSTER");
								System.out.println("MSG DESTINATION ID"+req.getGlobalHeader().getDestinationId());
								System.out.println("MY NODE ID"+sq.getState().getConf().getNodeId());
								if (req.getGlobalHeader().getDestinationId() == sq.getState().getConf().getNodeId()) {
									new Query(sq).handle(req);
								} else {
									System.out.println("NOT FOR MY NODE ID");
//									for (EdgeInfo ei : sq.getState().getEmon().getOutboundEdgeInfoList()) {
//										if (ei.getRef() == req.getGlobalHeader().getDestinationId()) {
//											if (ei.getChannel().isActive()) {
//												ei.getChannel().writeAndFlush(req);
//											}
//										}
//									}

//									Common.Failure.Builder cf = Common.Failure.newBuilder();
//									cf.setId(1);
//									cf.setMessage("File " + req.getRequest().getFile().getFilename() + " not found.");
//
//									Common.Response.Builder crb = Common.Response.newBuilder();
//									crb.setRequestId(req.getRequest().getRequestId());
//									crb.setRequestType(req.getRequest().getRequestType());
//									crb.setSuccess(false);
//									crb.setFailure(cf);
//
//									Global.GlobalHeader.Builder ghb = Global.GlobalHeader.newBuilder();
//									ghb.setClusterId(((Global.GlobalMessage) msg).getGlobalHeader().getClusterId());
//									ghb.setDestinationId(((Global.GlobalMessage) msg).getGlobalHeader().getDestinationId());
//									ghb.setTime(req.getGlobalHeader().getTime());
//
//									Global.GlobalMessage.Builder gm = Global.GlobalMessage.newBuilder();
//									gm.setGlobalHeader(ghb);
//									gm.setResponse(crb);



									Global.GlobalMessage.Builder gm = Global.GlobalMessage.newBuilder();

									Global.GlobalHeader.Builder ghb = Global.GlobalHeader.newBuilder();
									ghb.setClusterId(((PerChannelGlobalCommandQueue)sq).getState().getGlobalConf().getClusterId());
									ghb.setDestinationId(sq.getState().getConf().getNodeId());
									ghb.setTime(System.currentTimeMillis());
//                            ghb.setClusterId(((PerChannelWorkQueue)sq).getState().getConf().getClusterId());
//                            ghb.setDestinationId(((PerChannelWorkQueue)sq).getState().getConf().getNodeId());

									gm.setRequest(req.getRequest());
									gm.setGlobalHeader(ghb);
									sq.getState().getGemon().pushMessagesIntoCluster(gm.build());
								}
							} else {
								new Query(sq).handle(req);
//								sq.getState().getGemon().pushMessagesIntoCluster(req);
							}
						}else {
							System.out.println("I AM NOT A LEADER SO I WON'T PROCESS");
						}
					}



					//Has Response
					else if (req.hasResponse()) {
						if (checkIfLeader()) {
							if (verifyLocalOrGlobal(req)) {
								if (GlobalCommandHandler.globalClientChannel.containsKey(req.getResponse().getRequestId())) {
									Channel res = GlobalCommandHandler.globalClientChannel.get(req.getResponse().getRequestId());
									//GlobalCommandHandler.globalClientChannel.remove(msg.getPayload().getResponse().getRequestId());
									System.out.println("SENT BACK TO CLIENT");
									res.writeAndFlush(req);
								} else {
									logger.info("Request ID not found in Hashmap for Response - RequestId:" + req.getResponse().getRequestId());
								}
							} else {
								sq.getState().getGemon().pushMessagesIntoCluster(req);
							}
						}
					}


					//Has message
					else if (req.hasMessage()) {
						logger.info("Message is: " + req.getMessage());
					} else {
						logger.error("Unexpected message type. Yet to handle.");
					}
				}
			}catch (InterruptedException e) {
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


	public boolean checkClusterIdForOurRequest(Global.GlobalMessage req){
		System.out.println("MSG CLUSTER ID" + req.getGlobalHeader().getClusterId());
		System.out.println("MY CLUSTER ID" + sq.getState().getGlobalConf().getClusterId());
		if(req.getGlobalHeader().getClusterId() == sq.getState().getGlobalConf().getClusterId()){
			return true;
		} else {
			return false;
		}
	}

	public boolean checkIfLeader(){
		//RaftManager.getInstance().electionInstance() RaftManager.getInstance().electionInstance().isElectionInprogress() &&
		if(RaftManager.getInstance().getLeaderNode() == null){
			System.out.println("Leader not found.");
			return false;
		}
		if(sq.getState().getConf() == null){
			System.out.println("Conf not found.");
			return false;
		}
		if(RaftManager.getInstance().getLeaderNode() == sq.getState().getConf().getNodeId() ){
			return true;
		}
		return false;
	}

	public boolean verifyLocalOrGlobal(Global.GlobalMessage message){
		//if(((Global.GlobalMessage) msg).getGlobalHeader().getDestinationId())
		boolean check = false;
		for(RoutingConf.RoutingEntry e : sq.getRoutingConf().getRouting()){
			if(e.getId() == message.getGlobalHeader().getDestinationId()){
				check = true;
				break;
			}
		}
		if(message.getGlobalHeader().getDestinationId() == sq.getState().getConf().getNodeId()){
			check = true;
		}
		return check;
	}

}