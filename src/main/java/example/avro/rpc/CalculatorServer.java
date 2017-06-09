package example.avro.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.avro.ipc.specific.SpecificResponder; 


public class CalculatorServer { 
	  
    public static class CalculatorImpl implements Calculator { 
 
        @Override 
        public double add(double x, double y) throws AvroRemoteException { 
            return x + y; 
        } 
 
        @Override 
        public double subtract(double x, double y) throws AvroRemoteException { 
            return x - y; 
        } 
    } 
 
    private static Server calcServer; 
    private static final int CALC_SERVER_PORT = 65123; 
 
    private static void startServer() throws IOException { 
        calcServer = new NettyServer(new SpecificResponder(Calculator.class, new CalculatorImpl()), 
                                     new InetSocketAddress(CALC_SERVER_PORT)); 
    } 
 
    public static void main(String[] args) throws Exception { 
 
        System.out.println("Starting server..."); 
        startServer(); 
        System.out.println("Server started"); 
        
        Thread.sleep(100 * 1000);
        calcServer.close(); 
    } 
} 
