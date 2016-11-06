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
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;

public class WorkOutboundAppWorker extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("woaw:server");

	int workerId;
	PerChannelWorkQueue sq;
	boolean forever = true;

	public WorkOutboundAppWorker(ThreadGroup tgrp, int workerId, PerChannelWorkQueue sq) {
		super(tgrp, "outboundWork-" + workerId);
		this.workerId = workerId;
		this.sq = sq;

		if (sq.outbound == null)
			throw new RuntimeException("connection worker detected no outboundWork queue");
	}

	@Override
	public void run() {
		Channel conn = sq.channel;
		if (conn == null || !conn.isOpen()) {
			PerChannelWorkQueue.logger.error("connection missing, no outboundWork communication");
			return;
		}

		while (true) {
			if (!forever && sq.outbound.size() == 0)
				break;

			try {
				// block until a message is enqueued
				GeneratedMessage msg = sq.outbound.take();
				if (conn.isWritable()) {
					boolean rtn = false;
					if (sq.channel != null && sq.channel.isOpen() && sq.channel.isWritable()) {
						
						ChannelFuture cf = sq.channel.writeAndFlush((Work.WorkRequest)msg);

						logger.info("Server--sending -- work-- response");
						// blocks on write - use listener to be async
						cf.awaitUninterruptibly();
						logger.debug("Written to channel");
						rtn = cf.isSuccess();
						if (!rtn) {
							logger.error("Sending failed " + rtn
									+ "{Reason:" + cf.cause() + "}");
							sq.outbound.putFirst(msg);
						}
						else
							logger.info("Message Sent");
					}

				} else
					sq.outbound.putFirst(msg);
			} catch (InterruptedException ie) {
				break;
			} catch (Exception e) {
				logger.error("Unexpected communcation failure", e);
				break;
			}
		}

		if (!forever) {
			logger.debug("connection queue closing");
		}
	}
}
