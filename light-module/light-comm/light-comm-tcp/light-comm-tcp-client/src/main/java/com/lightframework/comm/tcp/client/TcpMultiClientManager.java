package com.lightframework.comm.tcp.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TcpMultiClientManager {

    private EventLoopGroup group;

    static final String CLIENT_ID = "CLIENT_ID";

    private final ConcurrentHashMap<String, TcpClient> CLIENTS = new ConcurrentHashMap<>();

    public TcpMultiClientManager(int workerCount){
        this.group = new NioEventLoopGroup(workerCount);
    }

    public TcpClient createClient(TcpClientConfig clientConfig){
        TcpClient client = new TcpClient(clientConfig, group,true);
        return client;
    }

    public void addClient(String id,TcpClient client){
        CLIENTS.put(id,client);
        client.setId(id);
    }

    public void removeClient(String id){
        TcpClient tcpClient = CLIENTS.remove(id);
        if(tcpClient != null){
            tcpClient.unsetId();
        }
    }

    public void removeClient(TcpClient client){
        Iterator<Map.Entry<String, TcpClient>> iterator = CLIENTS.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, TcpClient> next = iterator.next();
            if(next.getValue() == client){
                client.unsetId();
                iterator.remove();
                break;
            }
        }
    }

    public static String getClientId(Channel channel){
        Attribute<Object> attr = channel.attr(AttributeKey.valueOf(CLIENT_ID));
        return attr == null?null:(String)attr.get();
    }

    public TcpClient getClient(String id){
        return CLIENTS.get(id);
    }

    public int getClientCount(){
        return CLIENTS.size();
    }

    public void destroy(){
        try {
            group.shutdownGracefully().sync();
            CLIENTS.clear();
        } catch (InterruptedException e) {
        }
    }
}
