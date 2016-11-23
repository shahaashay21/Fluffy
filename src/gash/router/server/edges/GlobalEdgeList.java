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

import java.util.HashMap;
import java.util.HashSet;

public class GlobalEdgeList {
	protected HashMap<String, GlobalEdgeInfo> map = new HashMap<String, GlobalEdgeInfo>();

	public GlobalEdgeList() {

	}

	public synchronized GlobalEdgeInfo createIfNew(String ref, int id, int port) {
		if (hasNode(ref))
			return getNode(ref);
		else
			return addNode(ref, id, port);
	}

	public synchronized GlobalEdgeInfo addNode(String ref, int id, int port) {
		if (!verify(ref, id, port)) {
			// TODO log error
			throw new RuntimeException("Invalid node info");
		}

		if (!hasNode(ref)) {
			GlobalEdgeInfo ei = new GlobalEdgeInfo(ref, id, port);
			map.put(ref, ei);
			return ei;
		} else
			return null;
	}

	private boolean verify(String ref, int host, int port) {
		if ( ref == null || port < 1024)
			return false;
		else
			return true;
	}

	public boolean hasNode(String ref) {
		return map.containsKey(ref);

	}

	public synchronized GlobalEdgeInfo getNode(String ref) {
		return map.get(ref);
	}

	public synchronized void removeNode(String ref) {
			map.remove(ref);
	}

	public synchronized void clear() {
		map.clear();
	}
	public synchronized GlobalEdgeInfo returnAndRemoveIfNotNew(String ref, String host, int port) {
		if (hasNode(ref)) {
			GlobalEdgeInfo ei = getNode(ref);
			removeNode(ei.getRef());
			return ei;
		}
		else {
			return null;
		}
	}

	public synchronized HashSet<GlobalEdgeInfo> getEdgeInfoListFromMap(){
		return new HashSet<GlobalEdgeInfo>(map.values());
	}

	public synchronized void addEdge(GlobalEdgeInfo ei){
		if(!map.containsKey(ei.getRef()))
			map.put(ei.getRef(),ei);
	}

	public synchronized boolean isEdge(EdgeInfo ei){
		return map.containsKey(ei.getRef());
	}
}
