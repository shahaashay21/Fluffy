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
import gash.router.server.MessageServer;
import gash.router.server.PrintUtil;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.election.RaftManager;
import gash.router.server.listener.EdgeDisconnectionListener;
import gash.router.server.resources.Ping;
import gash.router.server.resources.Query;
import gash.router.server.resources.Response;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

public class WorkInboundAppWorker extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("wiaw:server");

	int workerId;
	PerChannelWorkQueue sq;
	boolean forever = true;

	public WorkInboundAppWorker(ThreadGroup tgrp, int workerId, PerChannelWorkQueue sq) {
		super(tgrp, "inboundWork-" + workerId);
		this.workerId = workerId;
		this.sq = sq;

		if (sq.inbound == null)
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
			if (!forever && sq.inbound.size() == 0)
				break;

			try {
				// block until a message is enqueued
				GeneratedMessage msg = sq.inbound.take();

				// process request and enqueue response

				if (msg instanceof Work.WorkRequest) {
					Work.WorkRequest req = ((Work.WorkRequest) msg);
					Work.Payload payload = req.getPayload();
					boolean msgDropFlag;

					//PrintUtil.printWork(req);
					if (payload.hasBeat()) {
						//Work.Heartbeat hb = payload.getBeat();
						logger.info("heartbeat from " + req.getHeader().getNodeId());
						EdgeMonitor emon = MessageServer.getEmon();
						EdgeInfo ei = new EdgeInfo(req.getHeader().getNodeId(),"",req.getHeader().getSource());
						ei.setChannel(sq.getChannel());
						emon.addToInbound(ei);
						RaftManager.getInstance().assessCurrentState();
					} else if (payload.hasPing()) {
						/*logger.info("ping from <node,host> : <" + req.getHeader().getNodeId() + ", " + req.getHeader().getSourceHost()+">");
						PrintUtil.printWork(req);
						if(req.getHeader().getDestination() == sq.state.getConf().getNodeId()){
							//handle message by self
							logger.info("Ping for me: " + " from "+ req.getHeader().getSourceHost());

							Work.WorkRequest.Builder rb = Work.WorkRequest.newBuilder();

							Common.Header.Builder hb = Common.Header.newBuilder();
							hb.setNodeId(sq.state.getConf().getNodeId());
							hb.setTime(System.currentTimeMillis());
							hb.setDestination(Integer.parseInt(req.getHeader().getSourceHost().substring(req.getHeader().getSourceHost().lastIndexOf('_')+1)));
							hb.setSourceHost(req.getHeader().getSourceHost().substring(req.getHeader().getSourceHost().indexOf('_')+1));
							hb.setDestinationHost(req.getHeader().getSourceHost());
							hb.setMaxHops(5);

							rb.setHeader(hb);
							rb.setSecret(1234567809);
							rb.setPayload(Work.Payload.newBuilder().setPing(true));
							//channel.writeAndFlush(rb.build());
							sq.enqueueResponse(rb.build(),sq.getChannel());
						}
						else { //message doesn't belong to current node. Forward on other edges
							msgDropFlag = true;
							PrintUtil.printWork(req);
							if (req.getHeader().getMaxHops() > 0 && MessageServer.getEmon() != null) {// forward if Comm-worker port is active
								for (EdgeInfo ei : MessageServer.getEmon().getOutboundEdgeInfoList()) {
									if (ei.isActive() && ei.getChannel() != null) {// check if channel of outbound edge is active
										logger.debug("Workmessage being queued");
										Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

										Common.Header.Builder hb = Common.Header.newBuilder();
										hb.setNodeId(sq.state.getConf().getNodeId());
										hb.setTime(req.getHeader().getTime());
										hb.setDestination(req.getHeader().getDestination());
										hb.setSourceHost(sq.state.getConf().getNodeId()+"_"+req.getHeader().getSourceHost());
										hb.setDestinationHost(req.getHeader().getDestinationHost());
										hb.setMaxHops(req.getHeader().getMaxHops() -1);

										wb.setHeader(hb);
										wb.setSecret(1234567809);
										wb.setPayload(Work.Payload.newBuilder().setPing(true));
										//ei.getChannel().writeAndFlush(wb.build());
										PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
										edgeQueue.enqueueResponse(wb.build(),ei.getChannel());
										msgDropFlag = false;
										logger.debug("Workmessage queued");
									}
								}
								if (msgDropFlag)
									logger.info("Message dropped <node,ping,destination>: <" + req.getHeader().getNodeId() + "," + payload.getPing() + "," + req.getHeader().getDestination() + ">");
							} else {// drop the message or queue it for limited time to send to connected node
								//todo
								logger.info("No outbound edges to forward. To be handled");
							}
						}*/
						new Ping(sq).handle(req);

					}else if (payload.hasQuery()) {
						logger.debug("Query message on work channel from " + req.getHeader().getNodeId());
						new Query(sq).handle(req);
					}
					else if (payload.hasResponse()) {
						logger.debug("Response message on work channel from " + req.getHeader().getNodeId());
						new Response(sq).handle(req);
					}
					else if (payload.hasErr()) {
						Common.Failure err = payload.getErr();
						logger.error("failure from " + req.getHeader().getNodeId());
						// PrintUtil.printFailure(err);
					} else if (payload.hasTask()) {
						Work.Task t = payload.getTask();
						sq.gerServerState().getTasks().addTask(t);
					} else if (payload.hasState()) {
						Work.WorkState s = payload.getState();
					}
					else if(payload.hasRaftmsg())
					{
						RaftManager.getInstance().processRequest((Work.WorkRequest)msg);
						//ElectionManager.getInstance().assessCurrentState();
					}
				}
			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected processing failure", e);
				break;
			}
		}

		if (!forever) {
			logger.debug("connection queue closing");
		}
	}
}