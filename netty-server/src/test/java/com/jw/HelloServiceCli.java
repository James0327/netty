package com.jw;

import com.guoyy.netty.conf.HostInfo;
import com.jw.demo.HelloService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;

public class HelloServiceCli {
    public static void main(String[] args) throws TException {
        TSocket socket = new TSocket(HostInfo.rpcSrvIp, HostInfo.rpcSrvPort, 30000);
        TBinaryProtocol protocol = new TBinaryProtocol(socket);

        HelloService.Client client = new HelloService.Client(protocol);

        socket.open();

        String ret = client.sayHello("James.");
        System.out.println("ret: " + ret);

        socket.close();
    }

}
