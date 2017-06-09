package org.gbif.ecat.ws;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.util.Utf8;

public class NubLookupClient {
	  @SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {

		  NettyTransceiver client = new NettyTransceiver(
	          new InetSocketAddress("localhost", 7001));
	      
	      NubLookup proxy = (NubLookup) SpecificRequestor.getClient(NubLookup.class, client);
	      
	      Request req = new Request();
	      req.name = new Utf8("Puma");
	      System.out.println("Result: " + proxy.send(req).kingdomId);

	      client.close();
	  }

}
