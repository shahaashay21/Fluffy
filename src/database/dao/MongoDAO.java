package database.dao;

import com.mongodb.*;
import database.dbconnetor.MongoConnector;
import database.model.DataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by n on 4/3/16.
 */
public class MongoDAO {

    protected static Logger logger = LoggerFactory.getLogger("mongoDAO");
    private static HashSet<DataModel> imageOfData = new HashSet<>();

    public synchronized static int saveData(String collectionName, DataModel data){
        int result = 0;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            BasicDBObjectBuilder docBuilder = BasicDBObjectBuilder.start();

            docBuilder.append("name", data.getName());
            docBuilder.append("seqNumber", data.getSeqNumber());
            docBuilder.append("dataChunk", data.getDataChunk());

            DBObject doc = docBuilder.get();
            //result = dbCollection.insert(doc).getN();
            WriteResult returnResult = dbCollection.insert(doc);
            result = returnResult.getN()==0?1:0;
            imageOfData.add(data);
        }
        else{
            logger.error("Connection problem");
        }
        return result;
    }

    public synchronized static ArrayList<DataModel> getData(String collectionName, DataModel data){
        ArrayList<DataModel> result = null;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            DBObject query = BasicDBObjectBuilder.start().add("name", data.getName()).get();
            DBCursor cursor = dbCollection.find(query);
            if(cursor.size() > 0) {
                result = new ArrayList<>();

                while (cursor.hasNext()) {
                    DBObject dbObject = cursor.next();
                    ByteArrayOutputStream b= new ByteArrayOutputStream();
                    try {
                        new ObjectOutputStream(b).writeObject(dbObject.get("dataChunk"));
                        result.add(new DataModel(dbObject.get("name").toString(),Integer.parseInt(dbObject.get("seqNumber").toString()),
                                b.toByteArray()));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    b=null;
                }
            }
        }
        return result;
    }

    public synchronized static DataModel getSingleData(String collectionName, DataModel data){
        DataModel result = null;
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        if(dbCollection != null){
            DBObject query = BasicDBObjectBuilder.start().add("name", data.getName()).add("seqNumber",data.getSeqNumber()).get();
            DBCursor cursor = dbCollection.find(query);
            if(cursor.size() == 1) {
                while (cursor.hasNext()) {
                    DBObject dbObject = cursor.next();
                    ByteArrayOutputStream b= new ByteArrayOutputStream();
                    try {
                        new ObjectOutputStream(b).writeObject(dbObject.get("dataChunk"));
                        result = new DataModel(dbObject.get("name").toString(),Integer.parseInt(dbObject.get("seqNumber").toString()),
                                b.toByteArray());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    b=null;
                }
            }
        }
        return result;
    }

    public static HashSet<DataModel> getimageOfData(){
        return imageOfData;
    }

    public static void main(String[] args){
        //int result = MongoDAO.saveData("test",new DataModel("hello",1,"okay".getBytes()));
        //System.out.println(result);
        ArrayList<DataModel> result = MongoDAO.getData("test",new DataModel("SJSU.pdf",0,null));
        System.out.println(result.size());
    }

    public synchronized static boolean isSufficientSpace(String collectionName){
        DBCollection dbCollection = MongoConnector.connectToCollection(collectionName);
        CommandResult resultSet = dbCollection.getStats();

        //System.out.println(resultSet.get("storageSize").toString());
        return (((Integer)resultSet.get("storageSize") - (Integer)resultSet.get("size")) > 10240);
    }
}
