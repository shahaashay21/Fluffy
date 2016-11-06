package database.cluster_storage.model;

import org.codehaus.jackson.annotate.JsonValue;

/**
 * Created by a on 4/6/16.
 */
public class StoredFile {

    private String name;

    private int seqNumber;

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonValue
    public int getSeqNumber() {
        return seqNumber;
    }

    @Override
    public boolean equals(Object obj) {
        StoredFile storedFile = (StoredFile) obj;
        return (storedFile.getName().equals(this.getName()) && storedFile.getSeqNumber() == this.getSeqNumber());
    }
}
