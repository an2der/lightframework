package com.lightframework.comm.tcp.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpMultiClientManager {

    private EventLoopGroup group;

    private final ConcurrentHashMap<String, TcpClient> CLIENTS = new ConcurrentHashMap<>();

    public TcpMultiClientManager(int workerCount){
        this.group = new NioEventLoopGroup(workerCount);
    }

    public TcpClient createClient(String id,TcpClientConfig clientConfig){
        TcpClient client = new TcpClient(clientConfig, group,true);
        CLIENTS.put(id, client);
        return client;
    }

    public void removeClient(String id){
        CLIENTS.remove(id);
    }

    public void removeClient(TcpClient client){
        Iterator<Map.Entry<String, TcpClient>> iterator = CLIENTS.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, TcpClient> next = iterator.next();
            if(next.getValue() == client){
                iterator.remove();
                break;
            }
        }
    }

    public TcpClient getClient(String id){
        return CLIENTS.get(id);
    }

    public void destroy(){
        try {
            group.shutdownGracefully().sync();
            CLIENTS.clear();
        } catch (InterruptedException e) {
        }
    }
}
