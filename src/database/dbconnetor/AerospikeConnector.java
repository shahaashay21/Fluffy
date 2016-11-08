//package database.dbconnetor;
//
//import com.aerospike.client.AerospikeClient;
//import database.model.DBConfigModel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.UnknownHostException;
//import java.util.HashMap;
//import java.util.Properties;
//

//public class AerospikeConnector {
//    protected static Logger logger = LoggerFactory.getLogger("aerospike");
//
//    private static Properties configProperty;
//    private static InputStream inputStream;
//
//    private static HashMap<DBConfigModel,AerospikeClient> connectionMap= new HashMap<>();
//
//    public static AerospikeClient getConnection(){
//
//        AerospikeClient ac = null;
//
//        configProperty = new Properties();
////        inputStream = MongoConnector.class.getClassLoader().getResourceAsStream("aerospike-config.properties");
//
//        if(inputStream == null){
//            logger.error("Unable to load aerospike property file");
//        }
//        else {
//            try {
//                configProperty.load(inputStream);
//                DBConfigModel dbConfig = new DBConfigModel(configProperty.getProperty("host"), Integer.parseInt(configProperty.getProperty("port")));
//                if(connectionMap.containsKey(dbConfig))
//                    ac = connectionMap.get(dbConfig);
//                else {
//                    ac = new AerospikeClient(dbConfig.getHost(), dbConfig.getPort());
//                    connectionMap.put(dbConfig,ac);
//                }
//
//            } catch (UnknownHostException e) {
//                logger.error(e.getMessage());
//            } catch (IOException e) {
//                logger.error(e.getMessage());
//            }
//        }
//        return ac;
//    }
//
//    public static String getDatabaseName(){
//        return configProperty.getProperty("dbName");
//    }
//}
