/**
 * Copyright 2016 Gash.
 * <p/>
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.client;

import com.google.protobuf.ByteString;
import global.Global.GlobalHeader;
import global.Global.GlobalMessage;
import pipe.common.Common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * front-end (proxy) to our service - functional-based
 *
 * @author gash
 *
 */
public class MessageClient {
    // track requests
    private long curID = 0;

    public MessageClient(String host, int port) {
        init(host, port);
    }

    private void init(String host, int port) {
        CommConnection.initConnection(host, port);
    }

    public void addListener(CommListener listener) {
        CommConnection.getInstance().addListener(listener);
    }

    public void ping() {
        // construct the message to send
        GlobalHeader.Builder hb = createHeader(2, 12);

        GlobalMessage.Builder rb = GlobalMessage.newBuilder();
        rb.setGlobalHeader(hb);
        rb.setPing(true);

        try {
            CommConnection.getInstance().enqueue(rb.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void message(String message) {
        // construct the message to send
        GlobalHeader.Builder hb = createHeader(999, 1);

        GlobalMessage.Builder gmb = GlobalMessage.newBuilder();
        gmb.setGlobalHeader(hb);
        gmb.setMessage(message);

        try {
            // direct no queue
            // CommConnection.getInstance().write(rb.build());
            // using queue
            CommConnection.getInstance().enqueue(gmb.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void query(String value) {
        // construct the message to send
        GlobalHeader.Builder hb = createHeader(999, 5);

        Common.Request.Builder rb = Common.Request.newBuilder();
        rb.setRequestType(Common.RequestType.READ);
        rb.setFileName(value);

        GlobalMessage.Builder gmb = GlobalMessage.newBuilder();
        gmb.setGlobalHeader(hb);
        gmb.setRequest(rb);

        try {
            // direct no queue
            // CommConnection.getInstance().write(rb.build());
            // using queue
            CommConnection.getInstance().enqueue(gmb.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(String value) {
        // construct the message to send
        List<byte[]> arrayList = new ArrayList<>();
        GlobalHeader.Builder hb = createHeader(2, 12);
        Path path = Paths.get(value);
        try {
            byte[] data = Files.readAllBytes(path);
//            arrayList = ResourceUtil.divideArray(data,1048576);

            //NEW ONE
            arrayList = ResourceUtil.divideArray(data,1049576);
//            arrayList = ResourceUtil.divideArray(data,1010049576);
//            FileOutputStream fileOutputStream = new FileOutputStream("/Users/aashayshah/Documents/A/275/final-Netty/Files/"+ path.getFileName().toString(), true);
//            for(byte[] a : arrayList){
//                fileOutputStream.write(a);
//            }
//            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < arrayList.size(); i++)
        {
            Common.File.Builder fb = Common.File.newBuilder();
            System.out.println(path.getFileName().toString());
            fb.setFilename(path.getFileName().toString());
            fb.setData(ByteString.copyFrom(arrayList.get(i)));
            fb.setChunkId(i);
            fb.setTotalNoOfChunks(arrayList.size());
            Common.Request.Builder rb = Common.Request.newBuilder();
            rb.setRequestId("A1345");
            rb.setRequestType(Common.RequestType.WRITE);
            rb.setFileName(path.getFileName().toString());
            rb.setFile(fb);
            GlobalMessage.Builder gmb = GlobalMessage.newBuilder();
            gmb.setGlobalHeader(hb);
            gmb.setRequest(rb);

            try {
                // using queue
                CommConnection.getInstance().enqueue(gmb.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void read(String value) {
        // construct the message to send
        GlobalHeader.Builder hb = createHeader(2, 12);

        Common.File.Builder fb = Common.File.newBuilder();
        fb.setFilename(value);
        Common.Request.Builder rb = Common.Request.newBuilder();
        rb.setRequestType(Common.RequestType.READ);
        rb.setFileName(value);
        rb.setFile(fb);
        rb.setRequestId("1");
        GlobalMessage.Builder gmb = GlobalMessage.newBuilder();
        gmb.setGlobalHeader(hb);
        gmb.setRequest(rb);

        try {
            // using queue
            CommConnection.getInstance().enqueue(gmb.build());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void delete(String value){
        GlobalHeader.Builder hb = createHeader(999, 5);

        Common.File.Builder fb = Common.File.newBuilder();
        fb.setFilename(value);
        Common.Request.Builder rb = Common.Request.newBuilder();
        rb.setRequestType(Common.RequestType.DELETE);
        rb.setFileName(value);
        rb.setFile(fb);
        rb.setRequestId("1");
        GlobalMessage.Builder gmb = GlobalMessage.newBuilder();
        gmb.setGlobalHeader(hb);
        gmb.setRequest(rb);

        try {
            // using queue
            CommConnection.getInstance().enqueue(gmb.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void release() {
        CommConnection.getInstance().release();
    }

    /**
     * Since the service/server is asychronous we need a unique ID to associate
     * our requests with the server's reply
     *
     * @return
     */
    private synchronized long nextId() {
        return ++curID;
    }

    public static GlobalHeader.Builder createHeader(int cluster_id, int distination_id) {
        GlobalHeader.Builder hb = GlobalHeader.newBuilder();
        hb.setClusterId(cluster_id);
        hb.setTime(System.currentTimeMillis());
        hb.setDestinationId(distination_id);
        return hb;
    }
}
