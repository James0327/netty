package com.jw;

import com.guoyy.netty.conf.HostInfo;
import com.jw.demo.HelloService;
import com.jw.demo.impl.HelloServiceImpl;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

public class HelloServiceSrv {

    public static void main(String[] args) throws TTransportException {

        TProcessor processor = new HelloService.Processor<>(new HelloServiceImpl());
        TServerSocket serverSocket = new TServerSocket(HostInfo.rpcSrvPort);
        TServer.Args tArgs = new TServer.Args(serverSocket);

        tArgs.processor(processor);
        tArgs.protocolFactory(new TBinaryProtocol.Factory());

        TServer server = new TSimpleServer(tArgs);
        System.out.println("Running Server ...... ");
        server.serve();

    }

}
