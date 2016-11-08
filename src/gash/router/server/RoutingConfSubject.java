package gash.router.server;


public interface RoutingConfSubject {
    public void attach(RoutingConfObserver observer);
    public void detach(RoutingConfObserver observer);
    public void notifyObservers();

}
