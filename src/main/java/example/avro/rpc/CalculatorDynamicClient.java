package example.avro.rpc;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;

import org.apache.avro.Protocol;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.generic.GenericRequestor;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class CalculatorDynamicClient {

	static String avpr = "{"
			+"    \"namespace\": \"example.avro.rpc\","
			+"    \"protocol\": \"Calculator\","
			+"    \"types\": [],"
			+"    \"messages\": {"
			+"        \"add\": {"
			+"                \"request\":[ { \"name\": \"x\", \"type\": \"double\"},"
			+"                            { \"name\": \"y\", \"type\": \"double\"} ],"
			+"                \"response\":\"double\""
			+"            },"
			+"          \"subtract\": {"
			+"                \"request\":[ { \"name\": \"x\", \"type\": \"double\"},"
			+"                            { \"name\": \"y\", \"type\": \"double\"} ],"
			+"                \"response\":\"double\""
			+"          }"
			+"    }"
			+"}";
	public static void main(String[] args) throws Exception { 
	    // Client Start
		double x = 2;
		double y = 2;
		
		String method = "add";
		
		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("x", x);
		paramMap.put("y", x);
		
		Protocol protocol = Protocol.parse(avpr);
		NioClientSocketChannelFactory channelFactory = null;
		NettyTransceiver nettyTrans = null;
        try {
        	// Server 가 떠있지않을 경우 Thread 가 clean되지 않으므로 여기서 생성하도록 한다
        	channelFactory = new NioClientSocketChannelFactory(
        			Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool()  
        			);
        	
        	nettyTrans = new NettyTransceiver(new InetSocketAddress("localhost", 65123), channelFactory);
        	
        	GenericRequestor requestor = new GenericRequestor(protocol, nettyTrans);
	        
	        GenericRecord params = new GenericData.Record(protocol.getMessages()
	        		.get("add").getRequest());
	        
	        for(String key : paramMap.keySet()) {
	        	System.out.println("==> key = " + key);
	        	params.put(key, paramMap.get(key)); 
	        }
	        
	        Object response = requestor.request("add", params);
	        System.out.println(">>>>> result = " + response);
        }
        catch(Exception e) {
        	System.out.println("### Error = " + e.toString());
        	e.printStackTrace();
        }
        finally {
        	System.out.println("### finally....");
        	if(nettyTrans != null) {
        		System.out.println("### nettyTrans CLOSE = " +nettyTrans);
        		nettyTrans.close(); 
        	}
        	else {
        		if(channelFactory != null)	{
        			System.out.println("### channelFactory CLOSE = " +channelFactory);
        			channelFactory.releaseExternalResources();
        		}
            }
        }
	} 
}
