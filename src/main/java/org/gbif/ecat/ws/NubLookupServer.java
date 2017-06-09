package org.gbif.ecat.ws;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.specific.SpecificResponder;

public class NubLookupServer {
	private static NettyServer server;

	// A mock implementation
	public static class NubLookupImpl implements NubLookup {
		@SuppressWarnings("deprecation")
		public Response send(Request request) throws AvroRemoteException {
			Response r = new Response();
			r.kingdomId = 100;
			return r;
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		server = new NettyServer(new SpecificResponder(NubLookup.class, new NubLookupImpl()),
				new InetSocketAddress(7001));
		
		System.out.println("Sleep 30 secs");
		Thread.sleep(30 * 1000);
		
		server.close();
	}

}
