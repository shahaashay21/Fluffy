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
import gash.router.server.resources.Ping;
import gash.router.server.resources.Query;
import global.Global;
import io.netty.channel.Channel;
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

				// process request and enqueue response
				if(msg instanceof Global.GlobalCommandMessage){

					//PrintUtil.printCommand((Pipe.CommandRequest) msg);
					Global.GlobalCommandMessage req = ((Global.GlobalCommandMessage) msg);

					if (req.hasPing()) {
						/*logger.info("ping from " + req.getHeader().getNodeId());
						if (req.getHeader().getDestination() == sq.getRoutingConf().getNodeId()) {
							//handle message by self
							logger.info("Message for me: " + req.getMessage() + " from " + req.getHeader().getSourceHost());
						} else { //message doesn't belong to current node. Forward on other edges
							msgDropFlag = true;
							PrintUtil.printGlobalCommand((Global.GlobalCommandMessage) msg);
							if (MessageServer.getEmon() != null) {// forward if Comm-worker port is active
								for (EdgeInfo ei : MessageServer.getEmon().getOutboundEdgeInfoList()) {
									if (ei.isActive() && ei.getChannel() != null) {// check if channel of outboundWork edge is active
										Work.WorkRequest.Builder wb = Work.WorkRequest.newBuilder();

										Common.Header.Builder hb = Common.Header.newBuilder();
										hb.setNodeId(sq.getRoutingConf().getNodeId());
										hb.setTime(req.getHeader().getTime());
										hb.setDestination(req.getHeader().getDestination());
										hb.setSourceHost(sq.getRoutingConf().getNodeId() + "_" + req.getHeader().getSourceHost());
										hb.setDestinationHost(req.getHeader().getDestinationHost());
										hb.setMaxHops(5);

										wb.setHeader(hb);
										wb.setSecret(1234567809);
										wb.setPayload(Work.Payload.newBuilder().setPing(true));

										Work.WorkRequest work = wb.build();

										PerChannelWorkQueue edgeQueue = (PerChannelWorkQueue) ei.getQueue();
										edgeQueue.enqueueResponse(work, ei.getChannel());
										msgDropFlag = false;
										logger.info("Workmessage queued");
									}
								}
								if (msgDropFlag)
									logger.info("Message dropped <node,ping,destination>: <" + req.getHeader().getNodeId() + "," + req.getPing() + "," + req.getHeader().getDestination() + ">");
							} else {// drop the message or queue it for limited time to send to connected node
								//todo
								logger.info("No outbound edges to forward. To be handled");
							}

						}*/
						new Ping(sq).handle(req);
					}else if(req.hasQuery()){
						new Query(sq).handle(req);
					}
					else{
						logger.error("Unexpected message type. Yet to handle.");
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
			logger.info("Command incoming connection queue closing");
		}
	}
}