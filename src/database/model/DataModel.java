package database.model;
import java.util.Arrays;

public class DataModel {

    String fileName;
    byte[] data;
    int chunkId;
    int chunkCount;

    public DataModel(String fileName, byte[] data) {
        this.fileName = fileName;
        this.data = data;
    }

    public DataModel(String fileName, int chunkId, byte[] data) {
        this.fileName = fileName;
        this.data = data;
        this.chunkId = chunkId;
    }

    public DataModel(String fileName, int chunkId, byte[] data, int chunkCount) {
        this.fileName = fileName;
        this.data = data;
        this.chunkId = chunkId;
        this.chunkCount = chunkCount;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + chunkCount;
        result = prime * result + chunkId;
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataModel other = (DataModel) obj;
        if (chunkCount != other.chunkCount)
            return false;
        if (chunkId != other.chunkId)
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        if (fileName == null) {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        return true;
    }

}
