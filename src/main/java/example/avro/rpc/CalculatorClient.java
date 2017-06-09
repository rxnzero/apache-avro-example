package example.avro.rpc;

import java.net.InetSocketAddress;

import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

public class CalculatorClient {
	
	public static void main(String[] args) throws Exception { 
	    // Client Start
        NettyTransceiver calcClient = new NettyTransceiver(new InetSocketAddress("localhost", 65123)); 
        
        Calculator proxy = (Calculator) SpecificRequestor.getClient(Calculator.class, calcClient); 
 
        System.out.println("Client built, get proxy"); 
        
        System.out.println("add(2, 3)=" + proxy.add(2, 3)); 
        
        System.out.println("subtract(5, 1)=" + proxy.subtract(5, 1)); 
        
        calcClient.close(); 
    } 
	
}
