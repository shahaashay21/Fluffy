package gash.router.server.election;


public interface ElectionListener {
    void concludeWith(boolean success, Integer LeaderID);
}
