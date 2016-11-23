package gash.router.server;

import gash.router.container.GlobalConf;
import gash.router.container.RoutingConf;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.edges.GlobalEdgeMonitor;
import gash.router.server.tasks.TaskList;

public class ServerState implements RoutingConfObserver{
	private RoutingConf conf;
	private gash.router.container.GlobalConf globalConf;
	private EdgeMonitor emon;
	private GlobalEdgeMonitor gemon;
	private TaskList tasks;

	public RoutingConf getConf() {
		return conf;
	}

	public void setConf(RoutingConf conf) {
		this.conf = conf;
	}

	public GlobalConf getGlobalConf() {
		return globalConf;
	}

	public void setGlobalConf(GlobalConf conf) {
		this.globalConf = conf;
	}

	public EdgeMonitor getEmon() {
		return emon;
	}

	public void setEmon(EdgeMonitor emon) {
		this.emon = emon;
	}

	public GlobalEdgeMonitor getGemon() {
		return gemon;
	}

	public void setGemon(GlobalEdgeMonitor emon) {
		this.gemon = gemon;
	}

	public TaskList getTasks() {
		return tasks;
	}

	public void setTasks(TaskList tasks) {
		this.tasks = tasks;
	}

	/**
	 * Update state with latest confFile
	 *
	 *
	 */
	@Override
	public void updateRoutingConf(RoutingConf newConf){
		setConf(newConf);
		emon.updateState(this);
	}

}
