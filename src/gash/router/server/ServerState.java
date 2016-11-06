package gash.router.server;

import gash.router.container.RoutingConf;
import gash.router.server.edges.EdgeMonitor;
import gash.router.server.tasks.TaskList;

public class ServerState implements RoutingConfObserver{
	private RoutingConf conf;
	private EdgeMonitor emon;
	private TaskList tasks;

	public RoutingConf getConf() {
		return conf;
	}

	public void setConf(RoutingConf conf) {
		this.conf = conf;
	}

	public EdgeMonitor getEmon() {
		return emon;
	}

	public void setEmon(EdgeMonitor emon) {
		this.emon = emon;
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
	 * @author n
	 *
	 */
	@Override
	public void updateRoutingConf(RoutingConf newConf){
		setConf(newConf);
		emon.updateState(this);
	}

}
