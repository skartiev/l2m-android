package com.zuzex.look2meet.Log;

//import com.facebook.fb303.LogEntry;
//import com.facebook.fb303.scribe;
//
//import org.apache.thrift.TException;
//import org.apache.thrift.protocol.TBinaryProtocol;
//import org.apache.thrift.protocol.TField;
//import org.apache.thrift.protocol.TList;
//import org.apache.thrift.protocol.TMap;
//import org.apache.thrift.protocol.TMessage;
//import org.apache.thrift.protocol.TProtocol;
//import org.apache.thrift.protocol.TSet;
//import org.apache.thrift.protocol.TStruct;
//import org.apache.thrift.transport.TFramedTransport;
//import org.apache.thrift.transport.TSocket;
//import org.apache.thrift.transport.TTransport;
//import org.apache.thrift.transport.TTransportException;
//
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//import java.util.ListIterator;

/**
 * Created by sanchirkartiev on 31.07.14.
 */
public class Logging {
//    //static String host = "zuzdb11.zuzex.lan";
//    static String host = "192.168.1.248";
//    static int port = 1463;
//    String category;
//    String message;
//    List<LogEntry>listOfMessages;
//    public Logging(String key, String message)
//    {
//       this.category = key;
//       this.message = message;
//        listOfMessages = new ArrayList<LogEntry>();
//
//
//        addMessagesToList(key, message);
//        //InitConnectionWithSend();
//    }
//
//    public void SendMessage() {
//        TSocket socket = new TSocket(host,port);
//        TTransport transport = new TFramedTransport(socket);
//        TProtocol protocol = new TBinaryProtocol(transport,false,false);
//        scribe.Client client = new scribe.Client(protocol,protocol);
//        try {
//            transport.open();
//        } catch (TTransportException e) {
//            e.printStackTrace();
//        } catch (NetworkOnMainThreadException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//             client.Log(listOfMessages);
//         } catch (TTransportException e) {
//            e.printStackTrace();
//        } catch (TException e) {
//             e.printStackTrace();
//         } catch (Exception e) {
//            e.printStackTrace();
//        }
//        transport.close();
//    }
//
//    private void addMessagesToList(String key, String message) {
//        LogEntry entry = new LogEntry(key,message);
//        listOfMessages.add(entry);
//    }
//

}
