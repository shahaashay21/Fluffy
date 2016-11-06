package database.dbconnetor;

import com.aerospike.client.AerospikeClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import database.model.DBConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by n on 4/2/16.
 */
public class MongoConnector {

    protected static Logger logger = LoggerFactory.getLogger("mongo");

    private static Properties configProperty;
    private static InputStream inputStream;
    private static HashMap<DBConfigModel,MongoClient> connectionMap= new HashMap<>();

    private static MongoClient getConnection(){

        MongoClient mc = null;

        configProperty = new Properties();
        inputStream = MongoConnector.class.getClassLoader().getResourceAsStream("mongo-config.properties");

        if(inputStream == null){
            logger.error("Unable to load mongo property file");
        }
        else {

            try {
                configProperty.load(inputStream);
                DBConfigModel dbConfig = new DBConfigModel(configProperty.getProperty("host"), Integer.parseInt(configProperty.getProperty("port")));
                if(connectionMap.containsKey(dbConfig))
                    mc = connectionMap.get(dbConfig);
                else {
                    logger.info("Connecting Mongo to <host,port> :"+dbConfig.getHost()+dbConfig.getPort());
                    mc = new MongoClient(dbConfig.getHost(),dbConfig.getPort());
                    connectionMap.put(dbConfig,mc);
                }
            } catch (UnknownHostException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return mc;
    }

    public static DBCollection connectToCollection(String collectionName){
        DBCollection dbCollection = null;
        MongoClient mongo = getConnection();
        if(mongo != null){
            DB db = mongo.getDB(configProperty.getProperty("dbName"));
            if(db != null){
                return db.getCollection(collectionName);
            }
        }
        return dbCollection;
    }
}
