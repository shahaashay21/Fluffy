package gash.router.server;

/**
 * Created by n on 3/20/16.
 */
public interface RoutingConfSubject {
    public void attach(RoutingConfObserver observer);
    public void detach(RoutingConfObserver observer);
    public void notifyObservers();

}
