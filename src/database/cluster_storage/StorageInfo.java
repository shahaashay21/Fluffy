package database.cluster_storage;
import database.cluster_storage.model.StoredFile;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by a on 4/6/16.
 */
public class StorageInfo {

    private static String fileStoragePath = "filestorage/storage.json";

    private static ObjectMapper mapper = new ObjectMapper();

    private static ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static boolean updateFile(byte[] fileBytes) {
        try {
            FileOutputStream fos = new FileOutputStream(fileStoragePath);
            fos.write(fileBytes);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fileExistInCluster(StoredFile file,int nodeID){

        try {
            Map<Integer,List<StoredFile>> myObjects = mapper.readValue(new FileReader(fileStoragePath), new TypeReference<Map<Integer,List<StoredFile>>>(){});

            List<StoredFile> storedInfo = myObjects.get(nodeID);

                for (StoredFile aStoredInfo : storedInfo)
                    if (aStoredInfo.equals(file)) {
                        return true;
                    }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static int fileExistInWhichNode(StoredFile file){

        int nodeId = -999;

        try {
            HashMap<Integer, List<StoredFile>> myObjects = mapper.readValue(new FileReader(fileStoragePath), new TypeReference<Map<Integer, List<StoredFile>>>() {
            });

            for (Map.Entry<Integer, List<StoredFile>> entry : myObjects.entrySet()) {
                Integer key = entry.getKey();

                List<StoredFile> storedInfo = myObjects.get(key);

                for (StoredFile aStoredInfo : storedInfo) {
                    if (aStoredInfo.equals(file)) {
                        nodeId = key;
                        break;
                    }
                }
                if(nodeId != -999){
                    break;
                }
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
        return nodeId;
    }

    public static void updateData(StoredFile file, int nodeID){

        try {
            HashMap<Integer, List<StoredFile>> myObjects = mapper.readValue(new FileReader(fileStoragePath), new TypeReference<Map<Integer, List<StoredFile>>>() {
            });

            for (Map.Entry<Integer, List<StoredFile>> entry : myObjects.entrySet()) {
                Integer key = entry.getKey();

                if(key == nodeID) {
                    List<StoredFile> storedInfo = myObjects.get(key);
                    storedInfo.add(file);
                }
            }
            mapper.writeValue(new File(fileStoragePath),myObjects);


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
