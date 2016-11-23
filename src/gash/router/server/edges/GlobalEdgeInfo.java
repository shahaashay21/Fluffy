/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server.edges;

import gash.router.server.queue.ChannelQueue;
import io.netty.channel.Channel;

public class GlobalEdgeInfo {
	private String ref;
	private int id1;
	private int port;
	private long lastHeartbeat = -1;
	private boolean active = false;
	private Channel channel;
	private ChannelQueue queue;
	private boolean clientChannel;

	public GlobalEdgeInfo(String ref, int id, int port) {
		this.ref = ref;
		this.id1 = id;
		this.port = port;
		this.channel = null;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public int getId() {
		return id1;
	}

	public void setId(int id) {
		this.id1 = id;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public ChannelQueue getQueue() {
		return queue;
	}

	public void setQueue(ChannelQueue queue) {
		this.queue = queue;
	}

	public boolean isClientChannel() {
		return clientChannel;
	}

	public void setClientChannel(boolean clientChannel) {
		this.clientChannel = clientChannel;
	}

	public boolean equals(Object x){
		GlobalEdgeInfo that = (GlobalEdgeInfo) x;
		if(this.ref.equals(that.ref) && (this.id1 == that.id1) && this.port == that.port){
			return true;
		}
		else{
			return false;
		}
	}

	//public int hashCode(){
		//ref + this.host.hashCode() + this.port;
	//}
}
