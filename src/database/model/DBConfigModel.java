package database.model;

/**
 * Created by n on 4/4/16.
 */
public class DBConfigModel {
    private String host;
    private int port;

    public DBConfigModel(String host, int port){
        this.host=host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean equals(Object x){
        DBConfigModel that = (DBConfigModel) x;
        if(this.host.equals(that.host) && this.port == that.port){
            return true;
        }
        else{
            return false;
        }
    }

    public int hashCode(){
        return this.host.hashCode() + this.port;
    }
}
