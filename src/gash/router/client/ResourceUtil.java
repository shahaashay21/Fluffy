package gash.router.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by a on 4/4/16.
 */
public class ResourceUtil {

    public static List<byte[]> divideArray(byte[] source, int chunksize) {

        List<byte[]> result = new ArrayList<>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }

        return result;
    }

}
