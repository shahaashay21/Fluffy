/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.app;


import gash.router.client.CommConnection;
import gash.router.client.CommListener;
import gash.router.client.MessageClient;
import global.*;
import routing.Pipe;

import java.io.FileOutputStream;
import java.util.Arrays;

public class DemoApp implements CommListener {
	private MessageClient mc;

	public DemoApp(MessageClient mc) {
		init(mc);
	}

	private void init(MessageClient mc) {
		this.mc = mc;
		this.mc.addListener(this);
	}

	private void ping(int N) {
		// test round-trip overhead (note overhead for initial connection)
		final int maxN = 10;
		long[] dt = new long[N];
		long st = System.currentTimeMillis(), ft = 0;
		for (int n = 0; n < N; n++) {
			mc.ping();
			ft = System.currentTimeMillis();
			dt[n] = ft - st;
			st = ft;
		}

		System.out.println("Round-trip ping times (msec)");
		for (int n = 0; n < N; n++)
			System.out.print(dt[n] + " ");
		System.out.println("");
	}

	private void message(String message) {
		long st = System.currentTimeMillis(), ft = 0,dt = 0;
			mc.message(message);
			ft = System.currentTimeMillis();
			dt = ft - st;
			st = ft;

		System.out.println("Round-trip message times (msec)");
		System.out.println(dt);
	}

	private void save(String value) {
		long st = System.currentTimeMillis(), ft = 0,dt = 0;
		mc.save(value);
		ft = System.currentTimeMillis();
		dt = ft - st;
		st = ft;

		System.out.println("Round-trip message times (msec)");
		System.out.println(dt);
	}

	private void read(String value) {
		long st = System.currentTimeMillis(), ft = 0,dt = 0;
		mc.read(value);
		ft = System.currentTimeMillis();
		dt = ft - st;
		st = ft;

		System.out.println("Round-trip message times (msec)");
		System.out.println(dt);
	}

	@Override
	public String getListenerID() {
		return "demo";
	}


	@Override
	public void onMessage(Global.GlobalMessage msg) {
		System.out.println("Got message from server");
		if(msg.getResponse().getRequestType().toString().equals("READ")){
			try {
				String newNameOfFile = (String) msg.getResponse().getFile().getFilename().toString();

				byte[] finalFile = msg.getResponse().getFile().getData().toByteArray();

				if(finalFile.length > 0 && newNameOfFile.length() > 0) {
					System.out.println("Length of answers is: " + finalFile.length);
					System.out.println(msg.getResponse().getFile().getData());
					System.out.println(msg.getResponse().getFile().getData().toString());
					System.out.println(msg.getResponse().getFile().getFilename());

//					FileOutputStream fileOutputStream = new FileOutputStream("/Users/aashayshah/Documents/A/275/final-Netty/Files/"+ newNameOfFile);
					FileOutputStream fileOutputStream = new FileOutputStream("/Users/aashayshah/Documents/A/275/final-Netty/Files/"+ newNameOfFile, true);
					fileOutputStream.write(finalFile);
					fileOutputStream.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}

		}else if(msg.getResponse().getRequestType().toString().equals("WRITE"))
			System.out.println("Result of data save request : Saved successfully---> " + msg.getResponse().getSuccess());
		else // for GET message response
			System.out.println("Final message action from server. Data needs to be parsed for --->" + msg.getResponse().getRequestType());
	}

	/**
	 * sample application (client) use of our messaging service
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		String host = args[0]; //127.0.0.1
		int port = Integer.parseInt(args[1]); //4568

		try {
			MessageClient mc = new MessageClient(host, port);
			DemoApp da = new DemoApp(mc);

			// do stuff w/ the connection
			//da.ping(2);
//			da.message("HEREE AASHAY !!!");
//			da.read("287-Macy.pdf");
			da.read("SampleVideo_2mb.mp4");
//			da.read("chapter2.pdf");
//			da.save("/Users/aashayshah/Desktop/287-Macy.pdf");
//			da.save("/Users/aashayshah/Desktop/SampleVideo_2mb.mp4");
//			da.save("/Users/aashayshah/Desktop/239-1.mov");
//			da.save("/Users/aashayshah/Desktop/chapter2.pdf");


			System.out.println("\n** exiting in 10 seconds. **");
			System.out.flush();
			Thread.sleep(10 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CommConnection.getInstance().release();
		}
	}
}
