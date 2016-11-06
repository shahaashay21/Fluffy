package gash.router.server.election;

/**
 * Created by r on 4/2/16.
 */
public interface ElectionListener {
    void concludeWith(boolean success, Integer LeaderID);
}
