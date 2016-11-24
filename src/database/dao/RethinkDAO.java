package database.dao;

import com.rethinkdb.net.Cursor;
import database.dbconnetor.RethinkConnector;
import database.model.DataModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aashayshah on 11/7/16.
 */
public class RethinkDAO {

    protected static Logger logger = LoggerFactory.getLogger("gciaw:server");

        private String document;
        RethinkConnector conn = new RethinkConnector("test");;

        public RethinkDAO(String document){
            this.document = document;
        }

        public Long count(JSONObject filter){
            if(filter != null){
                return RethinkConnector.r.table(document).filter(filter).count().run(conn.getConnection());
            }else{
                return RethinkConnector.r.table(document).count().run(conn.getConnection());
            }
        }

        public Object insert(JSONArray data){
            if(data != null){
                return RethinkConnector.r.table(document).insert(data).run(conn.getConnection());
            }else{
                return null;
            }
        }

        public Object insertFile(String fileName, int chunkId, int chunkCount,  byte[] file){
            if(file.length != 0){
//                return RethinkConnector.r.table(document).insert(RethinkConnector.r.hashMap("fileName", fileName).with("extension", extension).with("file", RethinkConnector.r.binary(file))).run(conn.getConnection());
                return RethinkConnector.r.table(document).insert(RethinkConnector.r.hashMap("fileName", fileName).with("chunkId", chunkId).with("chunkCount", chunkCount).with("file", RethinkConnector.r.binary(file))).run(conn.getConnection());
            }else{
                return null;
            }
        }

//        public Object updateFile(String fileName, int chunkId, int chunkCount,  byte[] file){
//            if(file.length != 0){
//                return RethinkConnector.r.table(document).filter({"fileName": fileName}).run();
//            }else{
//                return null;
//            }
//        }

        public Object delete(JSONObject data){
            if(data != null){
                return RethinkConnector.r.table(document).filter(data).delete().run(conn.getConnection());
            }else{
                return RethinkConnector.r.table(document).delete().run(conn.getConnection());
            }
        }

        public Object update(JSONArray compData, JSONArray newData){
            if(compData != null){
                return RethinkConnector.r.table(document).filter(compData).update(newData).run(conn.getConnection());
            }else{
                return RethinkConnector.r.table(document).update(newData).run(conn.getConnection());
            }
        }

        public Cursor fetch(JSONObject data){
            if(data != null){
                return RethinkConnector.r.table(document).filter(data).optArg("read_mode", "outdated").run(conn.getConnection());
            }else{
                return RethinkConnector.r.table(document).run(conn.getConnection());
            }
        }


        public ArrayList<DataModel> fetchFile(JSONObject data) throws IOException {
//            if(data != null){
//                return RethinkConnector.r.table(document).filter(data).pluck("fileName", "extension", "file").run(conn.getConnection());
//            }else{
//                return RethinkConnector.r.table(document).run(conn.getConnection());
//            }
            if(data != null){
                Cursor returnedData = RethinkConnector.r.table(document).filter(data).pluck("fileName", "chunkId", "file", "chunkCount").run(conn.getConnection());
                ArrayList<DataModel> returnArrayData = new ArrayList();;
                while(returnedData.hasNext()){

                    HashMap<String, Object> newData = new HashMap<String, Object>();
                    newData = (HashMap<String, Object>) returnedData.next();
                    String newNameOfFile = (String) newData.get("fileName");

                    byte[] finalFile = (byte[]) newData.get("file");
                    logger.info("GGGOOOTOTTT DDDAATTTAAA "+ finalFile.length);
                    returnArrayData.add(new DataModel(newData.get("fileName").toString(),Integer.parseInt(newData.get("chunkId").toString()), finalFile, Integer.parseInt(newData.get("chunkCount").toString())));
                }

                return returnArrayData;
            }else{
                return null;
            }
        }

}
