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
					//System.out.println("Got A work message");

					if (payload.hasBeat()) {
						//Work.Heartbeat hb = payload.getBeat();
						logger.info("heartbeat from " + req.getHeader().getNodeId());
						EdgeMonitor emon = MessageServer.getEmon();
						EdgeInfo ei = new EdgeInfo(req.getHeader().getNodeId(),"",req.getHeader().getSource());
						ei.setChannel(sq.getChannel());
						emon.addToInbound(ei);
						RaftManager.getInstance().assessCurrentState();
					} else if (payload.hasPing()) {
						System.out.println("Got A ping message at Work");
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
						logger.error("failure ping from " + req.getHeader().getNodeId());
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