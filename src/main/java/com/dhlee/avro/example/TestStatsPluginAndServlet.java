package com.dhlee.avro.example;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Random;

import javax.servlet.UnavailableException;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.Protocol;
import org.apache.avro.Protocol.Message;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.ipc.HttpServer;
import org.apache.avro.ipc.HttpTransceiver;
import org.apache.avro.ipc.LocalTransceiver;
import org.apache.avro.ipc.RPCContext;
import org.apache.avro.ipc.Responder;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.generic.GenericRequestor;
import org.apache.avro.ipc.generic.GenericResponder;
import org.apache.avro.ipc.stats.StatsPlugin;
import org.apache.avro.ipc.stats.StatsServer;
import org.apache.avro.ipc.stats.StatsServlet;
import org.mortbay.log.Log; 
 
public class TestStatsPluginAndServlet { 
  Protocol protocol = Protocol.parse("" + "{\"protocol\": \"Minimal\", " 
      + "\"messages\": { \"m\": {" 
      + "   \"request\": [{\"name\": \"x\", \"type\": \"int\"}], " 
      + "   \"response\": \"int\"} } }"); 
  Message message = protocol.getMessages().get("m"); 
 
  private static final long MS = 1000*1000L; 
 
  /** Returns an HTML string. */ 
  private String generateServletResponse(StatsPlugin statsPlugin) 
      throws IOException { 
    StatsServlet servlet; 
    try { 
      servlet = new StatsServlet(statsPlugin); 
    } catch (UnavailableException e1) { 
      throw new IOException(); 
    } 
    StringWriter w = new StringWriter(); 
    try { 
//      servlet.writeStats(w); 
    } catch (Exception e) { 
      e.printStackTrace(); 
    } 
    String o = w.toString(); 
    return o; 
  } 
 
  /** Expects 0 and returns 1. */ 
  static class TestResponder extends GenericResponder { 
    public TestResponder(Protocol local) { 
      super(local); 
    } 
 
    @Override 
    public Object respond(Message message, Object request) 
        throws AvroRemoteException { 
      return 1; 
    } 
 
  } 
 
  private void makeRequest(Transceiver t) throws IOException { 
    GenericRecord params = new GenericData.Record(protocol.getMessages().get( 
        "m").getRequest()); 
    params.put("x", 0); 
    GenericRequestor r = new GenericRequestor(protocol, t); 
  } 
 
  public void testFullServerPath() throws IOException { 
    Responder r = new TestResponder(protocol); 
    StatsPlugin statsPlugin = new StatsPlugin(); 
    r.addRPCPlugin(statsPlugin); 
    Transceiver t = new LocalTransceiver(r); 
 
    for (int i = 0; i < 10; ++i) { 
      makeRequest(t); 
    } 
    String o = generateServletResponse(statsPlugin); 
  } 
 

  public void testPayloadSize() throws IOException { 
    Responder r = new TestResponder(protocol); 
    StatsPlugin statsPlugin = new StatsPlugin(); 
    r.addRPCPlugin(statsPlugin); 
    Transceiver t = new LocalTransceiver(r); 
    makeRequest(t); 
     
    String resp = generateServletResponse(statsPlugin); 
  
  } 
   
  private RPCContext makeContext() { 
    RPCContext context = new RPCContext(); 
    context.setMessage(message); 
    return context; 
  } 
 
  /** Sleeps as requested. */ 
  private static class SleepyResponder extends GenericResponder { 
    public SleepyResponder(Protocol local) { 
      super(local); 
    } 
 
    @Override 
    public Object respond(Message message, Object request) 
        throws AvroRemoteException { 
    	long mili = 0;
      try { 
    	  mili = (Long)((GenericRecord)request).get("millis");
        Thread.sleep(mili); 
      } catch (InterruptedException e) { 
        throw new AvroRemoteException(e); 
      } 
      return "wait-"+mili; 
    } 
  } 
 
  /**   * Demo program for using RPC stats. This automatically generates 
   * client RPC requests. Alternatively a can be used (as below) 
   * to trigger RPCs. 
   * <pre> 
   * java -jar build/avro-tools-*.jar rpcsend '{"protocol":"sleepy","namespace":null,"types":[],"messages":{"sleep":{"request":[{"name":"millis","type":"long"}],"response":"null"}}}' sleep localhost 7002 '{"millis": 20000}' 
   * </pre> 
   * @param args 
   * @throws Exception 
   */ 
  public static void main(String[] args) throws Exception { 
    if (args.length == 0) { 
      args = new String[] { "7002", "7003" }; 
    } 
    Protocol protocol = Protocol.parse("{\"protocol\": \"sleepy\", " 
        + "\"messages\": { \"sleep\": {" 
        + "   \"request\": [{\"name\": \"millis\", \"type\": \"long\"}," + 
          "{\"name\": \"data\", \"type\": \"bytes\"}], " 
        + "   \"response\": \"string\"} } }"); 
    Log.info("Using protocol: " + protocol.toString()); 
    Responder r = new SleepyResponder(protocol); 
    StatsPlugin p = new StatsPlugin(); 
    r.addRPCPlugin(p); 
 
    // Start Avro server 
    HttpServer avroServer = new HttpServer(r, Integer.parseInt(args[0])); 
    avroServer.start(); 
 
    StatsServer ss = new StatsServer(p, 8080); 
     
    HttpTransceiver trans = new HttpTransceiver( 
        new URL("http://localhost:" + Integer.parseInt(args[0]))); 
    GenericRequestor req = new GenericRequestor(protocol, trans);  
 
    while(true) { 
      Thread.sleep(1000); 
      GenericRecord params = new GenericData.Record(protocol.getMessages().get("sleep").getRequest()); 
      Random rand = new Random(); 
      params.put("millis", Math.abs(rand.nextLong()) % 1000); 
      int payloadSize = Math.abs(rand.nextInt()) % 10000; 
      byte[] payload = new byte[payloadSize]; 
      rand.nextBytes(payload); 
      params.put("data", ByteBuffer.wrap(payload)); 
      Object response = req.request("sleep", params);
      System.out.println("RESPONSE = " +response);
    } 
  } 
}