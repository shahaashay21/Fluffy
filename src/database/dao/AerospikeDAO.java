//package database.dao;
//
//import com.aerospike.client.AerospikeClient;
//import com.aerospike.client.Bin;
//import com.aerospike.client.Key;
//import com.aerospike.client.Record;
//import com.aerospike.client.policy.BatchPolicy;
//import com.aerospike.client.policy.Policy;
//import com.aerospike.client.policy.RecordExistsAction;
//import com.aerospike.client.policy.WritePolicy;
//import database.dbconnetor.AerospikeConnector;
//import database.model.DataModel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//
///**
// * Created by n on 4/4/16.
// */
//public class AerospikeDAO {
//    protected static Logger logger = LoggerFactory.getLogger("aerospikeDAO");
//
//    public static int saveData(String collectionName, DataModel data){
//        int result = 0;
//        AerospikeClient client = AerospikeConnector.getConnection();
//        if(client != null){
//            WritePolicy wPolicy = new WritePolicy();
//            wPolicy.recordExistsAction = RecordExistsAction.UPDATE;
//
//            Key key = new Key(AerospikeConnector.getDatabaseName(), collectionName, data.getName()+":"+data.getSeqNumber());
//            Bin bin1 = new Bin("name", data.getName());
//            Bin bin2 = new Bin("seqNumber", data.getSeqNumber());
//            Bin bin3 = new Bin("dataChunk", data.getDataChunk());
//
//            client.put(wPolicy, key, bin1, bin2, bin3);
//        }
//        else{
//            logger.error("Connection not available");
//        }
//        return result;
//    }
//
//    public static DataModel getSingleData(String collectionName, DataModel data){
//        DataModel result = null;
//        Record record=null;
//        AerospikeClient client = AerospikeConnector.getConnection();
//        if(client != null){
//            Key searchKey = new Key(AerospikeConnector.getDatabaseName(), collectionName, data.getName()+":"+data.getSeqNumber());
//            record = client.get(null,searchKey);
//            if(record != null){
//                result = new DataModel(record.getValue("name").toString(),Integer.parseInt(record.getValue("seqNumber").toString()),record.getValue("dataChunk").toString().getBytes());
//            }
//        }
//        else{
//            logger.error("Connection not available");
//        }
//        return result;
//    }
//
//    public static ArrayList<DataModel> getData(String collectionName, DataModel data){
//        ArrayList<DataModel> result = null;
//        Record[] records=null;
//        AerospikeClient client = AerospikeConnector.getConnection();
//        if(client != null){
//            Key[] searchKeyArray = new Key[data.getSeqSize()];
//            for(int i=1;i<=data.getSeqSize();i++){
//                searchKeyArray[i] = new Key(AerospikeConnector.getDatabaseName(), collectionName, data.getName()+":"+i);
//            }
//            records = client.get((BatchPolicy) new Policy(),searchKeyArray);
//            if(records != null){
//                for(Record record : records)
//                    result.add(new DataModel(record.getValue("name").toString(),Integer.parseInt(record.getValue("seqNumber").toString()),record.getValue("dataChunk").toString().getBytes()));
//            }
//        }
//        else{
//            logger.error("Connection not available");
//        }
//        return result;
//    }
//}
