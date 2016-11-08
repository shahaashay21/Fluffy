package gash.router.server.election;

public class ElectionIDGenerator {
    private static int masterID = 0;

    public static synchronized int nextID() {
        if (masterID == Integer.MAX_VALUE) {
            masterID = 0;
        }
        return ++masterID;
    }

    public static synchronized void setMasterID(int id) {
        masterID = id;
    }
}
